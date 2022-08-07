package io.github.edy4c7.locationmapper.domains.services

import io.github.edy4c7.locationmapper.domains.entities.Upload
import io.github.edy4c7.locationmapper.domains.interfaces.storage.StorageClient
import io.github.edy4c7.locationmapper.domains.mapimagesources.MapImageSource
import io.github.edy4c7.locationmapper.domains.repositories.UploadRepository
import io.github.edy4c7.locationmapper.domains.valueobjects.Location
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
import kotlin.io.path.deleteIfExists
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
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
) {

    fun map(id: String, input: InputStream) {
        val sentences = BufferedReader(InputStreamReader(input)).use { br ->
            br.lines().filter { it.startsWith(JobLaunchingService.GPRMC_HEADER) }.toList()
        }

        val digits = (sentences.size * JobLaunchingService.FPS).toString().length

        val output = workDir.resolve("$id.zip")

        ZipOutputStream(output.outputStream()).use { zos ->
            var count = 0
            sentences.forEach {
                val tmp = Files.createTempFile(workDir, "", JobLaunchingService.IMAGE_SUFFIX)
                val location = Location.fromGprmc(it)

                mapSource.getMapImage(location).transferTo(tmp.outputStream())
                for (i in 1..JobLaunchingService.FPS) {
                    zos.putNextEntry(ZipEntry(String.format("%0${digits}d.${JobLaunchingService.IMAGE_SUFFIX}",
                        count++)))
                    tmp.inputStream().transferTo(zos)
                    zos.closeEntry()
                }

                tmp.deleteIfExists()
            }
        }

        val key = storageClient.upload(bucketName, output, "locationmapper.${output.extension}")

        uploadRepository.save(
            Upload(
                id = id,
                url = "$cdnOrigin/$key",
                expiredAt = LocalDateTime.now().plusHours(fileRetentionPeriod.toLong()),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
    }

    fun expire() {
        val targets = uploadRepository.findByExpiredAtLessThan(LocalDateTime.now())

        if (targets.isNotEmpty()) {
            val deleted = storageClient.delete(bucketName, *targets.map { "${it.id}.zip" }.toTypedArray())
            uploadRepository.deleteAllById(deleted.map { it.split(".")[0] })
        }
    }
}