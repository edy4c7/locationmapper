package io.github.edy4c7.locationmapper.config

import io.github.edy4c7.locationmapper.batch.steps.MappingTasklet
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.SimpleJobLauncher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import software.amazon.awssdk.services.s3.S3Client
import java.nio.file.Files
import java.nio.file.Path

@Configuration
class BatchConfig(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
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
    fun s3Client() : S3Client {
        return S3Client.create()
    }

    @Bean
    fun workDir(): Path {
        val path = Path.of(System.getProperty("java.io.tmpdir")).resolve("locationmapper")
        if (!Files.exists(path)) {
            Files.createDirectory(path)
        }
        return path
    }
}