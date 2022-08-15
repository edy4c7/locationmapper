package io.github.edy4c7.locationmapper.services

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.github.edy4c7.config.TestConfig
import io.github.edy4c7.locationmapper.domains.entities.Upload
import io.github.edy4c7.locationmapper.domains.interfaces.storage.StorageClient
import io.github.edy4c7.locationmapper.domains.mapimagesources.MapImageSource
import io.github.edy4c7.locationmapper.domains.repositories.UploadRepository
import io.github.edy4c7.locationmapper.domains.services.MappingService
import io.github.edy4c7.locationmapper.domains.valueobjects.Location
import io.mockk.*
import org.apache.commons.logging.Log
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.ZipInputStream
import kotlin.io.path.name

@SpringBootTest
@Import(TestConfig::class)
private class MappingServiceTests {
    companion object {
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

        val datetime: LocalDateTime = LocalDateTime.of(2022, 8, 12, 0, 12, 34)
    }

    @MockkBean(relaxed = true)
    private lateinit var mapImageSource: MapImageSource

    @MockkBean(relaxed = true)
    private lateinit var storageClient: StorageClient

    @MockkBean(relaxed = true)
    private lateinit var uploadRepository: UploadRepository

    @MockkBean(relaxed = true)
    private lateinit var log: Log

    @SpykBean
    private lateinit var workDir: Path

    @Value("\${storage.bucket}")
    private lateinit var bucketName: String

    @Value("\${cdn}")
    private lateinit var cdnOrigin: String

    @Value("\${fileRetentionPeriod}")
    private lateinit var fileRetentionPeriod: String

    @Autowired
    private lateinit var mappingService: MappingService

    @BeforeEach
    fun beforeEach() {
        mockkStatic(LocalDateTime::class)
        val counter = AtomicInteger()
        every { LocalDateTime.now() } answers {
            datetime.plusSeconds(counter.getAndIncrement().toLong())
        }

        mockkObject(Location)
    }

    @Test
    fun testMap() {
        every {
            Location.fromGprmc("\$GPRMC,044134.00,A,3514.97043,N,14000.09566,E,24.673,141.16,071121,,,D*5D")
        } returns Location(36.0, 140.0)
        every {
            Location.fromGprmc("\$GPRMC,044135.00,A,3514.96529,N,14000.10107,E,24.095,136.61,071121,,,D*51")
        } returns Location(37.0, 141.0)
        every {
            Location.fromGprmc("\$GPRMC,044136.00,A,3514.96053,N,14000.10647,E,22.937,134.39,071121,,,D*51")
        } returns Location(38.0, 142.0)
        every { mapImageSource.getMapImage(any()) } answers {
            val arg = firstArg<Location>()
            ByteArrayInputStream("${arg.latitude}, ${arg.longitude}".toByteArray())
        }

        val id = "abcd1234"
        val fileName = "$id.zip"
        val filePath = workDir.resolve(fileName)
        every { workDir.resolve(fileName) } returns filePath
        every { storageClient.upload(any(), any(), any(), any()) } answers { secondArg() }
        val ist = ByteArrayInputStream(sentences.toByteArray())
        every { uploadRepository.save(any()) } answers { firstArg() }

        mappingService.map(id, ist)

        verifyOrder {
            mapImageSource.getMapImage(Location(36.0, 140.0))
            mapImageSource.getMapImage(Location(37.0, 141.0))
            mapImageSource.getMapImage(Location(38.0, 142.0))
            storageClient.upload(bucketName, filePath.name, "locationmapper.zip", filePath)

            uploadRepository.save(Upload(
                id = id,
                url = "$cdnOrigin/${filePath.name}",
                expiredAt = datetime.plusHours(fileRetentionPeriod.toLong()),
                createdAt = datetime,
                updatedAt = datetime
            ))
        }

        val expects = arrayOf(
            "36.0, 140.0",
            "37.0, 141.0",
            "38.0, 142.0"
        )
        ZipInputStream(FileInputStream(filePath.toFile())).use { zis ->
            for ((i, e) in generateSequence { zis.nextEntry }.withIndex()) {
                assertEquals(String.format("%02d.png", i), e.name)
                val actual = ByteArray(11)
                zis.read(actual, 0, actual.size)
                assertEquals(expects[i / 30], String(actual), i.toString())
            }
        }
    }

    @Test
    fun testExpire() {
        val targets = listOf(
            Upload("1", "https://example.com/1",
                LocalDateTime.of(2022, 8, 11, 0, 12, 34)),
            Upload("2", "https://example.com/2",
                LocalDateTime.of(2022, 8, 11, 0, 12, 34))
        )
        every { uploadRepository.findByExpiredAtLessThan(datetime) } returns targets
        every { storageClient.delete(any(), *anyVararg()) } answers { secondArg<Array<String>>().toList() }

        mappingService.expire()

        verifyOrder {
            uploadRepository.findByExpiredAtLessThan(datetime)
            storageClient.delete(bucketName, "1.zip", "2.zip")
            uploadRepository.deleteAllById(listOf("1", "2"))
            log.info("2 files are deleted (${listOf("1.zip", "2.zip")}).")
        }
    }

    @Test
    fun testNoTargetExpire() {
        every { uploadRepository.findByExpiredAtLessThan(datetime) } returns listOf()

        mappingService.expire()

        verify { log.info("No files to delete.") }

        verifyOrder(inverse = true) {
            uploadRepository.findByExpiredAtLessThan(any())
            storageClient.delete(any(), *anyVararg())
            uploadRepository.deleteAllById(any())
        }
    }
}