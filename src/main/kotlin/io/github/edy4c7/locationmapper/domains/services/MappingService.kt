package io.github.edy4c7.locationmapper.domains.services

import io.github.edy4c7.locationmapper.domains.dto.JobProgress
import io.github.edy4c7.locationmapper.domains.entities.Mapping
import io.github.edy4c7.locationmapper.domains.interfaces.storage.StorageClient
import io.github.edy4c7.locationmapper.domains.repositories.MappingRepository
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.*
import kotlin.io.path.outputStream

@Service
class MappingService(
    private val jobLauncher: JobLauncher,
    private val jobExplorer: JobExplorer,
    private val mappingJob: Job,
    private val mappingRepository: MappingRepository,
    private val storageClient: StorageClient,
    private val workDir: Path,
    @Value("\${s3.bucket}") private val bucketName: String,
) {

    fun requestProcess(nmea: InputStream): String {
        val filePath = Files.createTempFile(workDir, "", ".nmea")
        val outFileName = filePath.fileName.toString().split(".")[0]
        nmea.transferTo(filePath.outputStream())
        val id = UUID.randomUUID()

        val execution = jobLauncher.run(
            mappingJob,
            JobParametersBuilder()
                .addString("mapping.id", id.toString())
                .addString("input.file.name", filePath.toString())
                .addString("output.file.name", workDir.resolve("$outFileName.zip").toString())
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

    fun getProgress(id: String) : JobProgress? {
        val buff = ByteBuffer.wrap(Base64.getUrlDecoder().decode(id))
        val uuid = UUID(buff.long, buff.long)
        return mappingRepository.findById(uuid.toString()).orElse(null)?.let {
            jobExplorer.getJobExecution(it.jobId)
        }?.executionContext?.let {
            JobProgress(
                id,
                if (it.containsKey("count.gprmc")) it.getInt("count.gprmc") else null,
                if (it.containsKey("count.completed")) it.getInt("count.completed") else 0
            )
        }
    }

    fun expire(retentionPeriod: Int) {
        val expireDate = LocalDateTime.now()
        val targets = mappingRepository.findByUploadedAtLessThan(expireDate)

        storageClient.delete(bucketName, *targets.map { "${it.id}.zip" }.toTypedArray())
    }
}