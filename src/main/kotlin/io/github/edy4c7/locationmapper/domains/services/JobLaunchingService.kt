package io.github.edy4c7.locationmapper.domains.services

import io.github.edy4c7.locationmapper.domains.entities.Mapping
import io.github.edy4c7.locationmapper.domains.repositories.MappingRepository
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.*
import kotlin.io.path.outputStream

@Service
class JobLaunchingService(
    private val jobLauncher: JobLauncher,
    private val jobExplorer: JobExplorer,
    private val mappingJob: Job,
    private val mappingRepository: MappingRepository,
    private val workDir: Path,
) {
    companion object {
        const val IMAGE_SUFFIX = ".png"
        const val FPS = 30
        const val GPRMC_HEADER = "\$GPRMC"
    }

    fun launchJob(nmea: InputStream): String {
        val filePath = Files.createTempFile(workDir, "", ".nmea")
        nmea.transferTo(filePath.outputStream())
        val id = UUID.randomUUID()

        val execution = jobLauncher.run(
            mappingJob,
            JobParametersBuilder()
                .addString("id", id.toString())
                .addString("input", filePath.toString())
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

    fun getJobProgress(id: String): BatchStatus? {
        val buff = ByteBuffer.wrap(Base64.getUrlDecoder().decode(id))
        val uuid = UUID(buff.long, buff.long)
        return mappingRepository.findById(uuid.toString()).orElse(null)?.let {
            jobExplorer.getJobExecution(it.jobId)?.status
        }
    }
}