package io.github.edy4c7.locationmapper.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

@TestConfiguration
internal class TestConfig {
    private val localStackImage = DockerImageName.parse("localstack/localstack:0.11.3")

    private val localStackContainer = LocalStackContainer(localStackImage).withServices(Service.S3)

    init {
        localStackContainer.start()
    }

    @Bean
    fun localStack(): LocalStackContainer = localStackContainer

    @Bean
    fun s3Client(localStack: LocalStackContainer): S3Client {
        return S3Client.builder()
            .endpointOverride(localStack.getEndpointOverride(Service.S3))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(localStack.accessKey, localStack.secretKey)
                )
            )
            .region(Region.of(localStackContainer.region))
            .build()
    }
}