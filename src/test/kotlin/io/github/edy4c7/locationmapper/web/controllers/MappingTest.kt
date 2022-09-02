package io.github.edy4c7.locationmapper.web.controllers

import org.junit.jupiter.api.Test
import org.mockserver.client.MockServerClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.containers.MockServerContainer
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@Testcontainers
private class MappingTest {
    @TestConfiguration
    class Configuration {
        @Bean
        fun s3Client(): S3Client {
            return S3Client.builder()
                .endpointOverride(localStackContainer.getEndpointOverride(Service.S3))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(localStackContainer.accessKey, localStackContainer.secretKey)
                    )
                )
                .region(Region.of(localStackContainer.region))
                .build()
        }
    }

    companion object {
        @JvmStatic
        val localStackImage: DockerImageName = DockerImageName.parse("localstack/localstack:0.11.3")

        @Container
        @JvmStatic
        val localStackContainer: LocalStackContainer = LocalStackContainer(localStackImage).withServices(Service.S3)

        @JvmStatic
        val mockServerImage: DockerImageName = DockerImageName.parse("mockserver/mockserver:5.14.0")

        @Container
        @JvmStatic
        val mockServerContainer = MockServerContainer(mockServerImage)
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val mockServer = MockServerClient(mockServerContainer.host, mockServerContainer.serverPort)

    @Test
    fun test() {

    }
}
