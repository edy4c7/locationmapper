package io.github.edy4c7.locationmapper.domains.repositories

import io.github.edy4c7.locationmapper.domains.entities.Upload
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

internal interface UploadRepository : CrudRepository<Upload, String> {
    @Suppress("UNUSED")
    fun findByExpiredAtLessThan(expiredAt: LocalDateTime): List<Upload>
}