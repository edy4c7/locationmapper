package io.github.edy4c7.locationmapper.batch.steps

import io.github.edy4c7.locationmapper.domains.services.MappingService
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
@StepScope
class ExpiringTasklet(
    private val service: MappingService,
    @Value("\${fileRetentionPeriod}") private val retentionPeriod: String,
) : Tasklet {
    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        service.expire(retentionPeriod.toInt())
        return RepeatStatus.FINISHED
    }
}