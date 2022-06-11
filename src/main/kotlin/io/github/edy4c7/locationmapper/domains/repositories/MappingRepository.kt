package io.github.edy4c7.locationmapper.domains.repositories

import io.github.edy4c7.locationmapper.domains.entities.Mapping
import org.springframework.data.repository.CrudRepository

interface MappingRepository : CrudRepository<Mapping, String>