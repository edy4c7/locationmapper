package io.github.edy4c7.locationmapper.domains.entities

import org.springframework.batch.core.BatchStatus
import org.springframework.data.annotation.Id

data class BatchJobStatus(
    @Id
    val mappingId: String,
    val status: BatchStatus,
)