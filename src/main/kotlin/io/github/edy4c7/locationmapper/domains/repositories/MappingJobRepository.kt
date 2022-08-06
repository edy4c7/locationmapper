package io.github.edy4c7.locationmapper.domains.repositories

import io.github.edy4c7.locationmapper.domains.entities.MappingJob
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface MappingJobRepository : Repository<MappingJob, String> {
    @Query("""
        SELECT
            *
        FROM
            MAPPING_JOB
        WHERE
            ID = :id
    """)
    fun findByMappingId(@Param("id") id: String): MappingJob
}