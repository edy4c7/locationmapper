package io.github.edy4c7.locationmapper.domains.entities

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import java.time.LocalDateTime

data class Mapping(
    @Id
    var id: String = "",
    var jobId: Long = 0,
    @Version
    var version: Long = 0,
    var createdAt: LocalDateTime = LocalDateTime.MIN,
    var updatedAt: LocalDateTime = LocalDateTime.MIN,
)