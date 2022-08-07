package io.github.edy4c7.locationmapper.batch.config

import io.github.edy4c7.locationmapper.batch.steps.ExpiringTasklet
import io.github.edy4c7.locationmapper.batch.steps.MappingTasklet
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Configuration for batch process
 */
@Configuration
@EnableBatchProcessing
@EnableScheduling
class BatchConfig(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
) {
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
        return jobBuilderFactory
            .get("expiringJob")
            .incrementer(RunIdIncrementer())
            .start(expiringStep)
            .build()
    }

    @Bean
    @JobScope
    fun expiringStep(expiringTasklet: ExpiringTasklet): Step {
        return stepBuilderFactory.get("expiring")
            .tasklet(expiringTasklet)
            .build()
    }
}