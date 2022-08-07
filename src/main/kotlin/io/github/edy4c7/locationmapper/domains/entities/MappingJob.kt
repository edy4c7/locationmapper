package io.github.edy4c7.locationmapper.domains.entities

import org.springframework.batch.core.BatchStatus
import org.springframework.data.annotation.Id

internal data class MappingJob(
    @Id
    val id: String,
    val status: BatchStatus,
    val url: String?,
)