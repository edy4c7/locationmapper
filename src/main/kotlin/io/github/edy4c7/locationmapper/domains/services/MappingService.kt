package io.github.edy4c7.locationmapper.domains.services

import io.github.edy4c7.locationmapper.domains.entities.Mapping
import io.github.edy4c7.locationmapper.domains.repositories.MappingRepository
import org.springframework.batch.core.*
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.*
import kotlin.io.path.outputStream

@Service
internal class MappingService(
    private val jobLauncher: JobLauncher,
    private val jobExplorer: JobExplorer,
    private val mappingJob: Job,
    private val mappingRepository: MappingRepository,
    private val workDir: Path,
) {

    fun requestProcess(nmea: InputStream) : String {
        val filePath = Files.createTempFile(workDir, "", ".nmea")
        val outFileName = filePath.fileName.toString().split(".")[0]
        nmea.transferTo(filePath.outputStream())
        val id = UUID.randomUUID()

        val ent = mappingRepository.save(
                Mapping(
                id = id.toString(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        jobLauncher.run(mappingJob,
            JobParametersBuilder()
                .addString("mapping.id", ent.id)
                .addString("input.file.name", filePath.toString())
                .addString("output.file.name", workDir.resolve("$outFileName.zip").toString())
                .toJobParameters()
         )

        val buff = ByteBuffer.wrap(ByteArray(16))
            .putLong(id.mostSignificantBits)
            .putLong(id.leastSignificantBits)

        return Base64.getUrlEncoder().encodeToString(buff.array())
    }

    fun getProgress(id: String) : BatchStatus? {
        val buff = ByteBuffer.wrap(Base64.getUrlDecoder().decode(id))
        val uuid = UUID(buff.long, buff.long)
        return mappingRepository.findById(uuid.toString()).map {
            jobExplorer.getJobExecution(it.jobId)?.status
        }.orElse(null)
    }
}