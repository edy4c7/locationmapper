package io.github.edy4c7.locationmapper.domains.repositories

import io.github.edy4c7.locationmapper.domains.entities.Mapping
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface MappingRepository : CrudRepository<Mapping, String> {
    @Suppress("UNUSED")
    fun findByUploadedAtLessThan(uploadedAt: LocalDateTime): Iterable<Mapping>
}