package io.github.edy4c7.locationmapper.domains.services

import io.github.edy4c7.locationmapper.common.utils.unwrap
import io.github.edy4c7.locationmapper.domains.interfaces.storage.StorageClient
import io.github.edy4c7.locationmapper.domains.mapimagesources.MapImageSource
import io.github.edy4c7.locationmapper.domains.repositories.MappingRepository
import io.github.edy4c7.locationmapper.domains.valueobjects.Location
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.deleteIfExists
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.streams.toList

@Service
class MappingService(
    private val mapSource: MapImageSource,
    private val storageClient: StorageClient,
    private val mappingRepository: MappingRepository,
    private val workDir: Path,
    @Value("\${storage.bucket}") private val bucketName: String,
) {

    fun map(id: String, input: InputStream): URL {
        val sentences = BufferedReader(InputStreamReader(input)).use { br ->
            br.lines().filter { it.startsWith(JobLaunchingService.GPRMC_HEADER) }.toList()
        }

        val digits = (sentences.size * JobLaunchingService.FPS).toString().length

        val output = workDir.resolve("$id.zip")

        ZipOutputStream(output.outputStream()).use { zos ->
            var count = 0
            sentences.forEach {
                val tmp = Files.createTempFile(workDir, "", JobLaunchingService.IMAGE_SUFFIX)
                val location = Location.fromGprmc(it)

                mapSource.getMapImage(location).transferTo(tmp.outputStream())
                for (i in 1..JobLaunchingService.FPS) {
                    zos.putNextEntry(ZipEntry(String.format("%0${digits}d.${JobLaunchingService.IMAGE_SUFFIX}",
                        count++)))
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