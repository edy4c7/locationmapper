package io.github.edy4c7.locationmapper.domains.services

import io.github.edy4c7.locationmapper.domains.repositories.JobApiCredentialRepository
import org.springframework.batch.core.*
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.outputStream

@Service
internal class BatchJobService(
    private val jobLauncher: JobLauncher,
    private val jobExplorer: JobExplorer,
    private val mappingJob: Job,
    private val credentialRepository: JobApiCredentialRepository,
    private val workDir: Path,
) {

    fun requestProcess(nmea: InputStream) : String {
        val filePath = Files.createTempFile(workDir, "", ".nmea")
        val outFileName = filePath.fileName.toString().split("0")[0]
        nmea.transferTo(filePath.outputStream())
        val token = UUID.randomUUID().toString()
        jobLauncher.run(mappingJob,
            JobParametersBuilder()
                .addString("token", token)
                .addString("input.file.name", filePath.toString())
                .addString("output.file.name", workDir.resolve("$outFileName.zip").toString())
                .toJobParameters()
         )

        return token
    }

    fun getProgress(token: String) : BatchStatus? {
        val cred = credentialRepository.findByToken(token)
        return jobExplorer.getJobExecution(cred.jobId)?.status
    }
}