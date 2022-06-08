package io.github.edy4c7.locationmapper.domains.services

import io.github.edy4c7.locationmapper.domains.entities.Request
import io.github.edy4c7.locationmapper.domains.repositories.RequestsRepository
import io.github.edy4c7.locationmapper.domains.valueobjects.JobStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameter
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.*
import kotlin.io.path.outputStream

@Service
internal class RequestingService(
    private val jobLauncher: JobLauncher,
    private val mappingJob: Job,
    private val workDir: Path,
    private val requestsRepository: RequestsRepository
) {

    fun requestProcess(nmea: InputStream) : Request {
        val id = UUID.randomUUID().toString()
        val filePath = workDir.resolve("$id.nmea")
        nmea.transferTo(filePath.outputStream())
        val request = Request(
            id = id,
            status = JobStatus.RESERVED,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
        requestsRepository.save(request)
        jobLauncher.run(mappingJob,
            JobParameters(mapOf(
                "request.id" to JobParameter(request.id),
                "input.file.name" to JobParameter(filePath.toString()),
                "output.file.name" to JobParameter(workDir.resolve("$id.zip").toString())
            )))
        return request
    }
}