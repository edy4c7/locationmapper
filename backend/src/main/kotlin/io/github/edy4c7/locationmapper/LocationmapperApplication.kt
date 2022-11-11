package io.github.edy4c7.locationmapper

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
private class LocationmapperApplication

fun main(args: Array<String>) {
	runApplication<LocationmapperApplication>(*args)
}
