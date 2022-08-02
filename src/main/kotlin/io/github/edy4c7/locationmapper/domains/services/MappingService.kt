package io.github.edy4c7.locationmapper.domains.services

import io.github.edy4c7.locationmapper.common.utils.unwrap
import io.github.edy4c7.locationmapper.domains.entities.Mapping
import io.github.edy4c7.locationmapper.domains.interfaces.storage.StorageClient
import io.github.edy4c7.locationmapper.domains.mapimagesources.MapImageSource
import io.github.edy4c7.locationmapper.domains.repositories.MappingRepository
import io.github.edy4c7.locationmapper.domains.valueobjects.Location
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.deleteIfExists
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.streams.toList

@Service
class MappingService(
    private val jobLauncher: JobLauncher,
    private val jobExplorer: JobExplorer,
    private val mappingJob: Job,
    private val mappingRepository: MappingRepository,
    private val mapSource: MapImageSource,
    private val storageClient: StorageClient,
    private val workDir: Path,
    @Value("\${s3.bucket}") private val bucketName: String,
) {
    companion object {
        const val suffix = ".png"
        const val FPS = 30
        const val GPRMC_HEADER = "\$GPRMC"
    }

    fun requestProcess(nmea: InputStream): String {
        val filePath = Files.createTempFile(workDir, "", ".nmea")
        nmea.transferTo(filePath.outputStream())
        val id = UUID.randomUUID()

        val execution = jobLauncher.run(
            mappingJob,
            JobParametersBuilder()
                .addString("mapping.id", id.toString())
                .addString("input.file.name", filePath.toString())
                .toJobParameters()
        )
        mappingRepository.save(
            Mapping(
                id = id.toString(),
                jobId = execution.id,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        val buff = ByteBuffer.wrap(ByteArray(16))
            .putLong(id.mostSignificantBits)
            .putLong(id.leastSignificantBits)

        return Base64.getUrlEncoder().encodeToString(buff.array())
    }

    fun getProgress(id: String): BatchStatus? {
        val buff = ByteBuffer.wrap(Base64.getUrlDecoder().decode(id))
        val uuid = UUID(buff.long, buff.long)
        return mappingRepository.findById(uuid.toString()).orElse(null)?.let {
            jobExplorer.getJobExecution(it.jobId)?.status
        }
    }

    fun map(id: String, input: InputStream): URL {
        val sentences = BufferedReader(InputStreamReader(input)).use { br ->
            br.lines().filter { it.startsWith(GPRMC_HEADER) }.toList()
        }

        val digits = (sentences.size * FPS).toString().length

        val output = workDir.resolve("$id.zip")

        ZipOutputStream(output.outputStream()).use { zos ->
            var count = 0
            sentences.forEach {
                val tmp = Files.createTempFile(workDir, "", suffix)
                val location = Location.fromGprmc(it)

                mapSource.getMapImage(location.latitude, location.longitude).transferTo(tmp.outputStream())
                for (i in 1..FPS) {
                    zos.putNextEntry(ZipEntry(String.format("%0${digits}d.${suffix}", count++)))
                    tmp.inputStream().transferTo(zos)
                    zos.closeEntry()
                }

                tmp.deleteIfExists()
            }
        }

        val url = storageClient.upload(bucketName, output, "locationmapper.${output.extension}")

        mappingRepository.findById(id).unwrap()?.let {
            it.uploadedAt = LocalDateTime.now()
            mappingRepository.save(it)
        }

        return url
    }

    fun expire(retentionDateTime: LocalDateTime) {
        val targets = mappingRepository.findByUploadedAtLessThan(retentionDateTime)

        storageClient.delete(bucketName, *targets.map { "${it.id}.zip" }.toTypedArray())
    }
}