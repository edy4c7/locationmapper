package io.github.edy4c7.locationmapper.domains.entities

import org.springframework.batch.core.BatchStatus
import org.springframework.data.annotation.Id

data class BatchJobStatus(
    @Id
    val id: String,
    val status: BatchStatus,
    val url: String?,
)