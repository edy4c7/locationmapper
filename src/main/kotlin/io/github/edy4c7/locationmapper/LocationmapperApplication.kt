package io.github.edy4c7.locationmapper

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableBatchProcessing
class LocationmapperApplication

fun main(args: Array<String>) {
	runApplication<LocationmapperApplication>(*args)
}
