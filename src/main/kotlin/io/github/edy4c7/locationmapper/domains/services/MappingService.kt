package io.github.edy4c7.locationmapper.domains.services

import io.github.edy4c7.locationmapper.domains.mapimagesources.MapImageSource
import io.github.edy4c7.locationmapper.domains.utils.toDegreeLatLng
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.deleteIfExists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.streams.toList

@Service
internal class MappingService(
    private val mapSource: MapImageSource,
    private val workDir: Path
) {

    companion object {
        const val suffix = ".png"
        const val FPS = 30
        const val GPRMC_HEADER = "\$GPRMC"
    }

    fun map(input: InputStream, output: OutputStream) {
        val sentences = BufferedReader(InputStreamReader(input)).use { br ->
            br.lines().filter { it.startsWith(GPRMC_HEADER) }.map { it.split(",") }.toList()
        }

        val digits = (sentences.size * FPS).toString().length

        ZipOutputStream(output).use { zos ->
            var count = 0
            sentences.forEach {
                val lat = toDegreeLatLng(it[4], it[3])
                val lng = toDegreeLatLng(it[6], it[5])
                val tmp = Files.createTempFile(workDir, "", suffix)

                mapSource.getMapImage(lat, lng).transferTo(tmp.outputStream())
                for (i in 1..FPS) {
                    zos.putNextEntry(ZipEntry(String.format("%0${digits}d.$suffix", count++)))
                    tmp.inputStream().transferTo(zos)
                    zos.closeEntry()
                }

                tmp.deleteIfExists()
            }
        }
    }
}