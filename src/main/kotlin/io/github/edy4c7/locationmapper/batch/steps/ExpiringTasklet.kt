package io.github.edy4c7.locationmapper.batch.steps

import io.github.edy4c7.locationmapper.domains.services.MappingService
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.stereotype.Component
import java.time.ZoneId

@Component
@StepScope
class ExpiringTasklet(
    private val service: MappingService,
) : Tasklet {
    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        contribution.stepExecution.jobExecution.jobParameters.getDate("retentionDateTime")?.let {
            service.expire(it.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
        }
        return RepeatStatus.FINISHED
    }
}