package io.github.edy4c7.locationmapper.domains.entities

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import java.time.LocalDateTime

internal data class Upload(
    @Id
    var id: String = "",
    var url: String = "",
    var expiredAt: LocalDateTime = LocalDateTime.MIN,
    @Version
    var version: Long = 0,
    var createdAt: LocalDateTime = LocalDateTime.MIN,
    var updatedAt: LocalDateTime = LocalDateTime.MIN,
)