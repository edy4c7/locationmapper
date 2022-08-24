package io.github.edy4c7.locationmapper.services

import io.github.edy4c7.locationmapper.domains.entities.MappingJob
import io.github.edy4c7.locationmapper.domains.repositories.MappingJobRepository
import io.github.edy4c7.locationmapper.domains.services.JobLaunchingService
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.outputStream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
private class JobLaunchingServiceTests {
    @MockK(relaxed = true)
    private lateinit var jobLauncher: JobLauncher

    @MockK(relaxed = true)
    private lateinit var mappingJob: Job

    @MockK(relaxed = true)
    private lateinit var jobRepository: MappingJobRepository

    @SpyK
    private var workDir = Path.of(System.getProperty("java.io.tmpdir")).resolve("location-mapper-test")

    @InjectMockKs
    private lateinit var service: JobLaunchingService

    @BeforeAll
    fun beforeAll() {
        MockKAnnotations.init(this)
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