package io.github.edy4c7.locationmapper.domains.repositories

import io.github.edy4c7.locationmapper.domains.entities.Request
import org.springframework.data.repository.CrudRepository

interface RequestsRepository : CrudRepository<Request, String>