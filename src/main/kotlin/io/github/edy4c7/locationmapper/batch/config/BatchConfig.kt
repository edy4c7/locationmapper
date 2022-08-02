package io.github.edy4c7.locationmapper.batch.config

import io.github.edy4c7.locationmapper.batch.steps.ExpiringTasklet
import io.github.edy4c7.locationmapper.batch.steps.MappingTasklet
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.*
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.SimpleJobLauncher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableBatchProcessing
@EnableScheduling
class BatchConfig(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
) : DefaultBatchConfigurer() {

    override fun createJobLauncher(): JobLauncher {
        val jobLauncher = SimpleJobLauncher()
        jobLauncher.setJobRepository(jobRepository)
        jobLauncher.setTaskExecutor(SimpleAsyncTaskExecutor())
        jobLauncher.afterPropertiesSet()
        return jobLauncher
    }

    @Bean
    fun mappingJob(mappingStep: Step): Job {
        return jobBuilderFactory.get("mappingJob").start(mappingStep).build()
    }

    @Bean
    @JobScope
    fun mappingStep(mappingTasklet: MappingTasklet): Step {
        return stepBuilderFactory.get("mapping")
            .tasklet(mappingTasklet)
            .build()
    }

    @Bean
    fun expiringJob(expiringStep: Step): Job {
        return jobBuilderFactory.get("expiringJob").start(expiringStep).build()
    }

    @Bean
    @JobScope
    fun expiringStep(expiringTasklet: ExpiringTasklet): Step {
        return stepBuilderFactory.get("expiring")
            .tasklet(expiringTasklet)
            .build()
    }
}