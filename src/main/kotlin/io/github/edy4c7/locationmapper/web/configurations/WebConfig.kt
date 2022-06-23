package io.github.edy4c7.locationmapper.web.configurations

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.http.HttpClient

@Configuration
class WebConfig {
    @Bean
    fun httpClient() : HttpClient {
        return  HttpClient.newHttpClient()
    }
}