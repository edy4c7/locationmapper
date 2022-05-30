package io.github.edy4c7.locationmapper.batch

import io.github.edy4c7.locationmapper.domains.tasklets.MappingTasklet
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path

@Configuration
class BatchConfig(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
) {
    @Bean
    fun mappingJob(mappingStep: Step): Job {
        return jobBuilderFactory.get("mappingJob").start(mappingStep).build()
    }

    @JobScope
    @Bean
    internal fun mappingStep(mappingTasklet: MappingTasklet): Step {
        return stepBuilderFactory.get("mapping")
            .tasklet(mappingTasklet)
            .build()
    }

    @StepScope
    @Bean
    internal fun input(@Value("#{jobParameters['input.file.name']}")name: String): InputStream {
        return FileInputStream(name)
    }

    @StepScope
    @Bean
    internal fun output(@Value("#{jobParameters['output.file.name']}")name: String): OutputStream {
        return FileOutputStream(name)
    }

    @Bean("workDir")
    fun workDir(): Path {
        val path = Path.of(System.getProperty("java.io.tmpdir")).resolve("locationmapper")
        if (!Files.exists(path)) {
            Files.createDirectory(path)
        }
        return path
    }
}