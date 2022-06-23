package io.github.edy4c7.locationmapper.domains.tasklets

import io.github.edy4c7.locationmapper.domains.repositories.MappingRepository
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@StepScope
class SaveMappingTasklet(private val mappingRepository: MappingRepository) : Tasklet {
    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        mappingRepository.findById(chunkContext.stepContext.jobParameters["mapping.id"].toString()).map {
            it.jobId = chunkContext.stepContext.jobInstanceId
            it.updatedAt = LocalDateTime.now()
            mappingRepository.save(it)
        }
        return RepeatStatus.FINISHED
    }
}