package io.github.edy4c7.locationmapper.services

import com.ninjasquad.springmockk.MockkBean
import io.github.edy4c7.config.TestConfig
import io.github.edy4c7.locationmapper.domains.entities.MappingJob
import io.github.edy4c7.locationmapper.domains.repositories.MappingJobRepository
import io.github.edy4c7.locationmapper.domains.services.JobLaunchingService
import io.mockk.*
import org.apache.commons.logging.Log
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.outputStream

@SpringBootTest
@Import(TestConfig::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
private class JobLaunchingServiceTests {
    @MockkBean(relaxed = true)
    private lateinit var jobLauncher: JobLauncher

    @MockkBean(name = "mappingJob", relaxed = true)
    private lateinit var mappingJob: Job

    @MockkBean(relaxed = true)
    private lateinit var jobRepository: MappingJobRepository

    @MockkBean(relaxed = true)
    private lateinit var log: Log

    @Autowired
    private lateinit var workDir: Path

    @Autowired
    private lateinit var service: JobLaunchingService

    @BeforeAll
    fun beforeAll() {
        mockkStatic(Files::class, UUID::class)
    }

    @AfterAll
    fun afterAll() {
        unmockkStatic(Files::class, UUID::class)
    }

    @Test
    fun testLaunch() {
        val id = "a12345b"
        val uuid = mockk<UUID>()
        every { uuid.toString() } returns id
        every { UUID.randomUUID() } returns uuid

        val filePath = spyk<Path>(Files.createTempFile(workDir, "", ".nmea"))
        val ost = mockk<OutputStream>(relaxed = true)
        every { filePath.outputStream() } returns ost
        every { Files.createTempFile(any<Path>(), any(), any()) } returns filePath
        val ist = mockk<InputStream>(relaxed = true)

        service.launchJob(ist)

        verify { ist.transferTo(ost) }
        val params = JobParametersBuilder()
            .addString("id", id)
            .addString("input", filePath.toString())
            .toJobParameters()
        verify { jobLauncher.run(mappingJob, params) }
    }

    @Test
    fun testGetJobProgress() {
        val id = "a1bcd1234"
        val expect = MappingJob(id, BatchStatus.STARTED)
        every { jobRepository.findByMappingId(any()) } returns expect

        val actual = service.getJobProgress(id)

        verify { jobRepository.findByMappingId(id) }
        assertEquals(expect, actual)
    }
}