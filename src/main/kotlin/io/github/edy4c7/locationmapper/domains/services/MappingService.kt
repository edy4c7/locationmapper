package io.github.edy4c7.locationmapper.domains.services

import com.github.guepardoapps.kulid.ULID
import io.github.edy4c7.locationmapper.domains.dao.RequestDao
import io.github.edy4c7.locationmapper.domains.entities.Request
import io.github.edy4c7.locationmapper.domains.valueobjects.JobStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.stereotype.Service

@Service
internal class MappingService(
    private val jobLauncher: JobLauncher,
    private val mappingJob: Job,
    private val requestDao: RequestDao
) {

    fun requestProcess() : Request {
        val request = Request()
        request.id = ULID.random()
        request.status = JobStatus.RESERVED
        requestDao.insert(request)
        jobLauncher.run(mappingJob, JobParameters())
        return request
    }
}