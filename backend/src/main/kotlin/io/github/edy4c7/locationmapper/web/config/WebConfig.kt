package io.github.edy4c7.locationmapper.web.config

import io.github.edy4c7.locationmapper.common.config.ApplicationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
private class WebConfig(private val props: ApplicationProperties) {
    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object: WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                props.allowedOrigins.forEach { registry.addMapping("/**").allowedOrigins(it) }
            }
        }
    }
}