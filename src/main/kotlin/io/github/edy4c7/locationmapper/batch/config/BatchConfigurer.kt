package io.github.edy4c7.locationmapper.batch.config

import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.SimpleJobLauncher
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.stereotype.Component

/**
 * Components factory for batch process
 */
@Component
class BatchConfigurer : DefaultBatchConfigurer() {
    override fun createJobLauncher(): JobLauncher {
        val jobLauncher = SimpleJobLauncher()
        jobLauncher.setJobRepository(jobRepository)
        jobLauncher.setTaskExecutor(SimpleAsyncTaskExecutor())
        jobLauncher.afterPropertiesSet()
        return jobLauncher
    }
}