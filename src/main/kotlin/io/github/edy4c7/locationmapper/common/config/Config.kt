package io.github.edy4c7.locationmapper.common.config

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.BeanCreationException
import org.springframework.beans.factory.InjectionPoint
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import software.amazon.awssdk.services.s3.S3Client
import java.net.http.HttpClient
import java.nio.file.Files
import java.nio.file.Path

@Configuration
private class Config {
    @Bean
    fun httpClient(): HttpClient {
        return HttpClient.newHttpClient()
    }

    @Bean
    fun s3Client(): S3Client {
        return S3Client.create()
    }

    @Bean
    fun workDir(@Value("\${workdir:locationmapper}") dirName: String): Path {
        val path = Path.of(System.getProperty("java.io.tmpdir")).resolve(dirName)
        if (!Files.exists(path)) {
            Files.createDirectory(path)
        }
        return path
    }

    @Bean
    @Scope("prototype")
    fun logger(injectionPoint: InjectionPoint): Log {
        val clazz = injectionPoint.methodParameter?.containingClass
            ?: injectionPoint.field?.declaringClass
            ?: throw BeanCreationException("could not initialize logger")

        return LogFactory.getLog(clazz)
    }
}