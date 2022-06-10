package io.github.edy4c7.locationmapper.domains.repositories

import io.github.edy4c7.locationmapper.domains.entities.JobApiCredential
import org.springframework.data.repository.CrudRepository

interface JobApiCredentialRepository : CrudRepository<JobApiCredential, String> {
    fun findByToken(token: String): JobApiCredential
}