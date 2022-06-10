package io.github.edy4c7.locationmapper.domains.tasklets

import io.github.edy4c7.locationmapper.domains.entities.JobApiCredential
import io.github.edy4c7.locationmapper.domains.repositories.JobApiCredentialRepository
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@StepScope
class SaveTokenTasklet(private val credentialRepository: JobApiCredentialRepository) : Tasklet {
    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        credentialRepository.save(
                JobApiCredential(
                jobId = chunkContext.stepContext.jobInstanceId,
                token = chunkContext.stepContext.jobParameters["token"].toString(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        return RepeatStatus.FINISHED
    }
}