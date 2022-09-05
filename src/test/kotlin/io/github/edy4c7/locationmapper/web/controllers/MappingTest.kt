package io.github.edy4c7.locationmapper.web.controllers

import io.github.edy4c7.locationmapper.infrastructures.MapQuestImageSource
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockserver.client.MockServerClient
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.testcontainers.containers.MockServerContainer
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import java.net.http.HttpClient
import java.util.*

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
private class MappingTest {
    @TestConfiguration
    class Configuration {
        companion object {
            val localStackImage: DockerImageName = DockerImageName.parse("localstack/localstack:0.11.3")
            val mockServerImage: DockerImageName = DockerImageName.parse("mockserver/mockserver:5.14.0")
        }

        private val localStackContainer: LocalStackContainer =
            LocalStackContainer(localStackImage).withServices(Service.S3)

        private val mockServerContainer = MockServerContainer(mockServerImage)

        init {
            localStackContainer.start()
            mockServerContainer.start()
        }

        @Bean
        fun localStackContainer(): LocalStackContainer = localStackContainer

        @Bean
        fun mockServerContainer(): MockServerContainer = mockServerContainer

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

        @Bean
        fun mapQuestImageSource(httpClient: HttpClient) = MapQuestImageSource(
            httpClient, mockServerContainer.endpoint, "TEST_KEY")
    }

    companion object {
        lateinit var onJobCompleted: () -> Unit

        val sentences = """
            ${'$'}GSENSOR,-64,30,16
            ${'$'}GSENSOR,-30,-22,60
            ${'$'}GSENSOR,10,-46,88
            ${'$'}GSENSOR,-28,62,2
            ${'$'}GSENSOR,-48,60,-8
            ${'$'}GPRMC,044134.00,A,3514.97043,N,14000.09566,E,24.673,141.16,071121,,,D*5D
            ${'$'}GPGGA,044134.00,3514.97043,N,14000.09566,E,2,08,1.18,66.0,M,39.9,M,,0000*69
            ${'$'}GPGSA,A,3,10,15,18,23,24,12,05,25,,,,,1.91,1.18,1.50*0D
            ${'$'}GSENSOR,-96,-16,78
            ${'$'}GSENSOR,-70,34,40
            ${'$'}GSENSOR,-14,28,18
            ${'$'}GSENSOR,-28,-4,4
            ${'$'}GSENSOR,-86,-8,12
            ${'$'}GPRMC,044135.00,A,3514.96529,N,14000.10107,E,24.095,136.61,071121,,,D*51
            ${'$'}GPGGA,044135.00,3514.96529,N,14000.10107,E,2,08,1.18,66.3,M,39.9,M,,0000*68
            ${'$'}GPGSA,A,3,10,15,18,23,24,12,05,25,,,,,1.91,1.18,1.50*0D
            ${'$'}GSENSOR,-80,-80,6
            ${'$'}GSENSOR,-34,-50,-40
            ${'$'}GSENSOR,-50,54,-22
            ${'$'}GSENSOR,-12,-22,-50
            ${'$'}GSENSOR,-66,-32,18
            ${'$'}GPRMC,044136.00,A,3514.96053,N,14000.10647,E,22.937,134.39,071121,,,D*51
            ${'$'}GPGGA,044136.00,3514.96053,N,14000.10647,E,2,08,1.18,66.7,M,39.9,M,,0000*64
            ${'$'}GPGSA,A,3,10,15,18,23,24,12,05,25,,,,,1.91,1.18,1.50*0D
        """.trimIndent()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var s3Client: S3Client

    @Autowired
    private lateinit var localStackContainer: LocalStackContainer

    @Autowired
    private lateinit var mockServerContainer: MockServerContainer

    @Value("\${bucketName}")
    private lateinit var bucketName: String

    @BeforeAll
    fun beforeAll() {
        s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build())
    }

    @Test
    fun test() {
        val responses = LinkedList<ByteArray>()
        responses.add(byteArrayOf(0x12, 0x34))
        responses.add(byteArrayOf(0x56, 0x78))
        responses.add(byteArrayOf(0x0a, 0x0b))

        val msc = MockServerClient(mockServerContainer.host, mockServerContainer.serverPort)
        msc.`when`(request().withPath("/"))
            .respond(response().withBody(responses.remove()))

        mockMvc.perform(multipart("/mapping")
            .file(MockMultipartFile("nmea", sentences.toByteArray())))

//        msc.verify(request().withPath("/"), VerificationTimes.exactly(responses.size))
    }
}
