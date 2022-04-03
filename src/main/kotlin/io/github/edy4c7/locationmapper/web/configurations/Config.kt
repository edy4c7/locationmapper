package io.github.edy4c7.locationmapper.web.configurations

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.http.HttpClient
import java.nio.file.Path

@Configuration
class Config {
    @Bean("workDir")
    fun workDir(): Path {
        return Path.of(System.getProperty("java.io.tmpdir")).resolve("locationmapper")
    }

    @Bean
    fun httpClient() : HttpClient {
        return  HttpClient.newHttpClient()
    }
}