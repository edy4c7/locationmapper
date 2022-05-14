package io.github.edy4c7.locationmapper.domains.entities

import io.github.edy4c7.locationmapper.domains.valueobjects.JobStatus
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import java.time.LocalDateTime

data class Request(
    @Id
    var id: String = "",
    var status: JobStatus = JobStatus.RESERVED,
    @Version
    var version: Long = 0,
    var createdAt: LocalDateTime = LocalDateTime.MIN,
    var updatedAt: LocalDateTime = LocalDateTime.MIN,
)