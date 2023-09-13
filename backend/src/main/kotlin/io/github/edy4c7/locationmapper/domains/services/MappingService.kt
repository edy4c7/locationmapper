package io.github.edy4c7.locationmapper.domains.services

import io.github.edy4c7.locationmapper.common.config.ApplicationProperties
import io.github.edy4c7.locationmapper.domains.entities.Progress
import io.github.edy4c7.locationmapper.domains.entities.Upload
import io.github.edy4c7.locationmapper.domains.interfaces.MapImageSource
import io.github.edy4c7.locationmapper.domains.interfaces.StorageClient
import io.github.edy4c7.locationmapper.domains.repositories.UploadRepository
import io.github.edy4c7.locationmapper.domains.valueobjects.Location
import org.apache.commons.logging.Log
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
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
    private val props: ApplicationProperties,
    private val log: Log,
) {
    companion object {
        const val IMAGE_SUFFIX = ".png"
        const val FPS = 30
        const val GPRMC_HEADER = "\$GPRMC"
    }

    @Async
    fun map(id: String, input: InputStream, sse: SseEmitter) {
        try {
            mapImpl(id, input, sse)
            sse.complete()
        } catch (e: Exception) {
            sse.completeWithError(e)
        }
    }

    private fun mapImpl(id: String, input: InputStream, sse: SseEmitter) {
        val sentences = BufferedReader(InputStreamReader(input)).use { br ->
            br.lines().filter { it.startsWith(GPRMC_HEADER) }.toList()
        }

        val total = sentences.count()
        sse.send(Progress(total, 0))

        val digits = (total * FPS).toString().length

        val output = workDir.resolve("$id.zip")

        ZipOutputStream(output.outputStream()).use { zos ->
            var count = 0
            for((index, elm) in sentences.withIndex()) {
                val tmp = Files.createTempFile(workDir, "", IMAGE_SUFFIX)
                val location = Location.fromGprmc(elm)

                mapSource.getMapImage(location).transferTo(tmp.outputStream())
                for (i in 1..FPS) {
                    zos.putNextEntry(ZipEntry(String.format("%0${digits}d${IMAGE_SUFFIX}",
                        count++)))
                    tmp.inputStream().transferTo(zos)
                    zos.closeEntry()
                }

                tmp.deleteIfExists()

                sse.send(Progress(total, index + 1))
            }
        }

        val key = storageClient.upload(props.bucketName, output.name,
            "locationmapper.${output.extension}", output)
        val timestamp = LocalDateTime.now()

        val url = "${props.cdnOrigin}/$key"
        uploadRepository.save(
            Upload(
                id = id,
                url = url,
                expiredAt = timestamp.plusHours(props.fileRetentionPeriod),
                createdAt = timestamp,
                updatedAt = timestamp
            )
        )
        sse.send(Progress(total, total, true, url))
    }

    fun expire() {
        val targets = uploadRepository.findByExpiredAtLessThan(LocalDateTime.now())

        if (targets.isEmpty()) {
            log.info("No files to delete.")
            return
        }

        val deleted = storageClient.delete(props.bucketName, *targets.map { "${it.id}.zip" }.toTypedArray())
        uploadRepository.deleteAllById(deleted.map { it.split(".")[0] })
        log.info("${deleted.size} files are deleted ($deleted).")
    }
}