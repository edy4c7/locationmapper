package io.github.edy4c7.locationmapper.domains.services

import io.github.edy4c7.locationmapper.domains.entities.Request
import io.github.edy4c7.locationmapper.domains.repositories.RequestsRepository
import io.github.edy4c7.locationmapper.domains.valueobjects.JobStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameter
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
internal class MappingService(
    private val jobLauncher: JobLauncher,
    private val mappingJob: Job,
    private val requestsRepository: RequestsRepository
) {

    fun requestProcess() : Request {
        val request = Request(
            id = UUID.randomUUID().toString(),
            status = JobStatus.RESERVED,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
        requestsRepository.save(request)
        jobLauncher.run(mappingJob,
            JobParameters(mapOf("request.id" to JobParameter(request.id ?: throw RuntimeException()))))
        return request
    }
}