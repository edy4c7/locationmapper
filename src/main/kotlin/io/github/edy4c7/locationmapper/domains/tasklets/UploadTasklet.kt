package io.github.edy4c7.locationmapper.domains.tasklets

import io.github.edy4c7.locationmapper.domains.interfaces.storage.StorageClient
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
@StepScope
class UploadTasklet(private val storageClient: StorageClient): Tasklet {
    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        val id = chunkContext.stepContext.jobParameters["mapping.id"].toString()
        val filePath = Path.of(chunkContext.stepContext.jobParameters["output.file.name"].toString())
        storageClient.upload(id, filePath)
        return RepeatStatus.FINISHED
    }
}