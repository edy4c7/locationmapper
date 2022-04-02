package io.github.edy4c7.locationmapper.domains.services

import io.github.edy4c7.locationmapper.domains.exceptions.MapImageSourceException
import io.github.edy4c7.locationmapper.domains.mapimagesources.MapImageSource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipInputStream
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

internal class MappingServiceTest {
    companion object{
        val imgs = arrayOf(byteArrayOf(0x12, 0x34), byteArrayOf(0x56, 0x78))
        val img1 = PipedInputStream()
        val img2 = PipedInputStream()
        val workDir: Path = Path.of(System.getProperty("java.io.tmpdir")).resolve("locationmapper")
        const val FPS = 30

        init {
            PipedOutputStream(img1).use { pos ->
                pos.write(imgs[0])
            }

            PipedOutputStream(img2).use { pos ->
                pos.write(imgs[1])
            }

            if (!workDir.exists()){
                Files.createDirectory(workDir)
            }
        }
    }

    @Test
    fun mapIsSuccess() {
        val imageSource = mock<MapImageSource> {
            on { getMapImage(eq(35.249268), eq(140.001861)) } doReturn img1
            on { getMapImage(eq(-35.249268), eq(-140.001861)) } doReturn img2
        }

        val service = MappingService(imageSource, workDir)

        PipedInputStream().use { input ->
            PipedOutputStream(input).use { pos ->
                PrintWriter(BufferedWriter(OutputStreamWriter(pos))).use {
                    it.println("\$GPRMC,044137.00,A,3514.95608,N,14000.11166,E,21.808,134.44,071121,,,D*5A")
                    it.println("\$GPRMC,044137.00,A,3514.95608,S,14000.11166,W,21.808,134.44,071121,,,D*5A")
                }
            }

            val zip = Files.createTempFile(workDir, "", ".zip")
            zip.outputStream().use{ output ->
                service.map(input, output)

                verify(imageSource)
                    .getMapImage(35.249268, 140.001861)
                verify(imageSource)
                    .getMapImage(-35.249268, -140.001861)
            }

            ZipInputStream(zip.inputStream()).use { zis ->
                var count = 0
                while(zis.nextEntry != null) {
                    val expect = imgs[(count * 10 / FPS) / 10]
                    val actual = ByteArray(expect.size)
                    zis.read(actual)
                    Assertions.assertArrayEquals(expect, actual)
                    zis.closeEntry()
                    count++
                }
            }
        }
    }

    @Test
    fun mapIsFailCausedByMapSourceException() {
        val mse = MapImageSourceException()
        val imageSource = mock<MapImageSource>{
            on { getMapImage(any(), any()) } doThrow mse
        }

        val service = MappingService(imageSource, workDir)

        PipedInputStream().use { input ->
            PipedOutputStream(input).use { pos ->
                PrintWriter(BufferedWriter(OutputStreamWriter(pos))).use {
                    it.println("\$GPRMC,044137.00,A,3514.95608,N,14000.11166,E,21.808,134.44,071121,,,D*5A")
                }
            }

            val zip = Files.createTempFile(workDir, "", ".zip")
            zip.outputStream().use { output ->
                val ex = assertThrows<MapImageSourceException> {
                    service.map(input, output)
                }
                assertEquals(mse, ex)
            }
        }
    }
}