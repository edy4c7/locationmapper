package io.github.edy4c7.locationmapper.config

import com.ninjasquad.springmockk.MockkBean
import org.springframework.boot.test.context.TestConfiguration
import software.amazon.awssdk.services.s3.S3Client
import java.net.http.HttpClient

@TestConfiguration
internal class TestConfig {
    @MockkBean(relaxed = true)
    lateinit var s3Client: S3Client

    @MockkBean(relaxed = true)
    lateinit var httpClient: HttpClient
}