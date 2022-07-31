package io.github.edy4c7.locationmapper.domains.services

import io.github.edy4c7.locationmapper.common.utils.unwrap
import io.github.edy4c7.locationmapper.domains.interfaces.storage.StorageClient
import io.github.edy4c7.locationmapper.domains.mapimagesources.MapImageSource
import io.github.edy4c7.locationmapper.domains.repositories.MappingRepository
import io.github.edy4c7.locationmapper.domains.utils.toDegreeLatLng
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.deleteIfExists
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.streams.toList

@Service
class MappingExecuteService(
    private val mapSource: MapImageSource,
    private val workDir: Path,
    @Value("\${s3.bucket}") private val bucketName: String,
    private val mappingRepository: MappingRepository,
    private val storageClient: StorageClient,
) {
    companion object {
        const val suffix = ".png"
        const val FPS = 30
        const val GPRMC_HEADER = "\$GPRMC"
    }

    fun map(id: String, input: InputStream): URL {
        val sentences = BufferedReader(InputStreamReader(input)).use { br ->
            br.lines().filter { it.startsWith(GPRMC_HEADER) }.map { it.split(",") }.toList()
        }

        val digits = (sentences.size * FPS).toString().length

        val output = workDir.resolve("$id.zip")

        ZipOutputStream(output.outputStream()).use { zos ->
            var count = 0
            sentences.forEach {
                val lat = toDegreeLatLng(it[4], it[3])
                val lng = toDegreeLatLng(it[6], it[5])
                val tmp = Files.createTempFile(workDir, "", suffix)

                mapSource.getMapImage(lat, lng).transferTo(tmp.outputStream())
                for (i in 1..FPS) {
                    zos.putNextEntry(ZipEntry(String.format("%0${digits}d.${suffix}", count++)))
                    tmp.inputStream().transferTo(zos)
                    zos.closeEntry()
                }

                tmp.deleteIfExists()
            }
        }

        val url = storageClient.upload(bucketName, output, "locationmapper.${output.extension}")

        mappingRepository.findById(id).unwrap()?.let {
            it.uploadedAt = LocalDateTime.now()
            mappingRepository.save(it)
        }

        return url
    }
}