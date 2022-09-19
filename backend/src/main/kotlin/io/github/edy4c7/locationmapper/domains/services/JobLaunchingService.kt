package io.github.edy4c7.locationmapper.domains.services

import io.github.edy4c7.locationmapper.domains.entities.MappingJob
import io.github.edy4c7.locationmapper.domains.repositories.MappingJobRepository
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.outputStream

@Service
internal class JobLaunchingService(
    private val jobLauncher: JobLauncher,
    private val mappingJob: Job,
    private val jobRepository: MappingJobRepository,
    private val workDir: Path,
) {

    fun launchJob(nmea: InputStream): String {
        val filePath = Files.createTempFile(workDir, "", ".nmea")
        nmea.transferTo(filePath.outputStream())
        val id = UUID.randomUUID().toString()

        jobLauncher.run(
            mappingJob,
            JobParametersBuilder()
                .addString("id", id)
                .addString("input", filePath.toString())
                .toJobParameters()
        )

        return id
    }

    fun getJobProgress(id: String): MappingJob? {
        return jobRepository.findByMappingId(id)
    }
}