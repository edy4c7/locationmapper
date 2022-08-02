package io.github.edy4c7.locationmapper.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.s3.S3Client
import java.net.http.HttpClient
import java.nio.file.Files
import java.nio.file.Path

@Configuration
class Config {
    @Bean
    fun httpClient(): HttpClient {
        return HttpClient.newHttpClient()
    }

    @Bean
    fun s3Client(): S3Client {
        return S3Client.create()
    }

    @Bean
    fun workDir(): Path {
        val path = Path.of(System.getProperty("java.io.tmpdir")).resolve("locationmapper")
        if (!Files.exists(path)) {
            Files.createDirectory(path)
        }
        return path
    }
}