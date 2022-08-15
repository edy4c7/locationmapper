package io.github.edy4c7.locationmapper.domains.services

import io.github.edy4c7.locationmapper.domains.entities.Upload
import io.github.edy4c7.locationmapper.domains.interfaces.MapImageSource
import io.github.edy4c7.locationmapper.domains.interfaces.StorageClient
import io.github.edy4c7.locationmapper.domains.repositories.UploadRepository
import io.github.edy4c7.locationmapper.domains.valueobjects.Location
import org.apache.commons.logging.Log
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.*
import kotlin.streams.toList

@Service
internal class MappingService(
    private val mapSource: MapImageSource,
    private val storageClient: StorageClient,
    private val uploadRepository: UploadRepository,
    private val workDir: Path,
    @Value("\${storage.bucket}") private val bucketName: String,
    @Value("\${cdn}") private val cdnOrigin: String,
    @Value("\${fileRetentionPeriod}") private val fileRetentionPeriod: String,
    private val log: Log,
) {
    companion object {
        const val IMAGE_SUFFIX = ".png"
        const val FPS = 30
        const val GPRMC_HEADER = "\$GPRMC"
    }

    fun map(id: String, input: InputStream) {
        val sentences = BufferedReader(InputStreamReader(input)).use { br ->
            br.lines().filter { it.startsWith(GPRMC_HEADER) }.toList()
        }

        val digits = (sentences.size * FPS).toString().length

        val output = workDir.resolve("$id.zip")

        ZipOutputStream(output.outputStream()).use { zos ->
            var count = 0
            sentences.forEach {
                val tmp = Files.createTempFile(workDir, "", IMAGE_SUFFIX)
                val location = Location.fromGprmc(it)

                mapSource.getMapImage(location).transferTo(tmp.outputStream())
                for (i in 1..FPS) {
                    zos.putNextEntry(ZipEntry(String.format("%0${digits}d${IMAGE_SUFFIX}",
                        count++)))
                    tmp.inputStream().transferTo(zos)
                    zos.closeEntry()
                }

                tmp.deleteIfExists()
            }
        }

        val key = storageClient.upload(bucketName, output.name,
            "locationmapper.${output.extension}", output)
        val timestamp = LocalDateTime.now()

        uploadRepository.save(
            Upload(
                id = id,
                url = "$cdnOrigin/$key",
                expiredAt = timestamp.plusHours(fileRetentionPeriod.toLong()),
                createdAt = timestamp,
                updatedAt = timestamp
            )
        )
    }

    fun expire() {
        val targets = uploadRepository.findByExpiredAtLessThan(LocalDateTime.now())

        if (targets.isEmpty()) {
            log.info("No files to delete.")
            return
        }

        val deleted = storageClient.delete(bucketName, *targets.map { "${it.id}.zip" }.toTypedArray())
        uploadRepository.deleteAllById(deleted.map { it.split(".")[0] })
        log.info("${deleted.size} files are deleted ($deleted).")
    }
}