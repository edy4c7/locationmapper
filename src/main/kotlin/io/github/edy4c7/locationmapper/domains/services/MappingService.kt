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
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.outputStream

@Service
internal class MappingService(
    private val mapSource: MapImageSource,
    private val workDir: Path
) {

    companion object {
        const val prefix = "map"
        const val suffix = ".png"
    }

    fun map(input: InputStream, output : OutputStream, width: Int = 1920, height: Int = 1080, fps: Int = 30) {
        val outDir = Files.createTempDirectory(workDir, null)

        BufferedReader(InputStreamReader(input)).use { br ->
            br.lines()
                .filter { it.startsWith("\$GPRMC") }
                .map { it.split(",") }
                .forEach {
                    val lat = toDegreeLatLng(it[4], it[3])
                    val lng = toDegreeLatLng(it[6], it[5])
                    Files.createTempFile(outDir, prefix, suffix).outputStream().use { s ->
                        mapSource.getMapImage(lat, lng, width = width, height = height).transferTo(s)
                    }
                }
        }

        var count = 0
        val files = outDir.listDirectoryEntries("*$suffix").sortedWith(compareBy { it.toFile().lastModified() })
        val digits = (files.count() * fps).toString().length
        ZipOutputStream(output).use { zos ->
            files.forEach { f ->
                for (i in 1..fps) {
                    zos.putNextEntry(ZipEntry(String.format("$prefix%0${digits}d.${f.toFile().extension}", count++)))
                    f.inputStream().use { ist -> zos.write(ist.readAllBytes()) }
                    zos.closeEntry()
                }
                f.deleteIfExists()
            }
        }

        outDir.deleteIfExists()
    }
}