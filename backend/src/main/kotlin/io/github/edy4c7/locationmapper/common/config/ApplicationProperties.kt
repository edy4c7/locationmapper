package io.github.edy4c7.locationmapper.common.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties
@ConstructorBinding
data class ApplicationProperties(
    val bucketName: String,
    val cdnOrigin: String,
    val fileRetentionPeriod: Long,
)