package io.github.edy4c7.locationmapper.domains.repositories

import io.github.edy4c7.locationmapper.domains.entities.BatchJobStatus
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface BatchJobStatusRepository : CrudRepository<BatchJobStatus, String> {
    @Query("""
        SELECT
            STRING_VAL MAPPING_ID,
            STATUS
        FROM BATCH_JOB_INSTANCE INSTNS
        LEFT OUTER JOIN BATCH_JOB_EXECUTION EXECUTION
	        ON INSTNS.JOB_INSTANCE_ID = EXECUTION.JOB_INSTANCE_ID
        LEFT OUTER JOIN BATCH_JOB_EXECUTION_PARAMS PARAMS
        	ON EXECUTION.JOB_EXECUTION_ID = PARAMS.JOB_EXECUTION_ID
        WHERE
            JOB_NAME = 'mappingJob'
            AND KEY_NAME = 'id' 
            AND STRING_VAL = :id
    """)
    fun findByMappingId(@Param("id") id: String): BatchJobStatus
}