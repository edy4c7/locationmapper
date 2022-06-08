package io.github.edy4c7.locationmapper.batch

import io.github.edy4c7.locationmapper.domains.mapimagesources.MapImageSource
import io.github.edy4c7.locationmapper.domains.valueobjects.LatAndLon
import org.springframework.batch.core.*
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.batch.item.file.transform.FieldSet
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.deleteIfExists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@Configuration
class BatchConfig(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
) {
    companion object {
        const val GPRMC_HEADER = "\$GPRMC"
        const val FPS = 30
        const val suffix = ".png"
    }

    @Bean
    fun mappingJob(countStep:Step, mappingStep: Step): Job {
        return jobBuilderFactory.get("mappingJob").start(countStep).next(mappingStep).build()
    }

    @JobScope
    @Bean
    fun countStep(@Value("#{jobExecution}")execution: JobExecution
        , @Value("#{jobParameters['request.id']}")id: String
        , @Value("#{jobParameters['input.file.name']}")inputFileName: String): Step {
        val counterTotal = AtomicInteger()
        val counterGprmc = AtomicInteger()
        return stepBuilderFactory.get("count")
            .chunk<FieldSet, FieldSet>(10)
            .reader(FlatFileItemReaderBuilder<FieldSet>()
                .name(id)
                .resource(FileSystemResource(inputFileName))
                .lineTokenizer(DelimitedLineTokenizer())
                .fieldSetMapper(PassThroughFieldSetMapper())
                .build())
            .writer {
                execution.executionContext.put("count.total", counterTotal.addAndGet(it.size))
                execution.executionContext.put("count.gprmc", counterGprmc.addAndGet(
                    it.stream().filter { e -> e.values[0] == GPRMC_HEADER }.count().toInt()))
            }
            .build()
    }

    @JobScope
    @Bean
    fun mappingStep(@Value("#{jobExecution}")execution: JobExecution
        , workDir: Path, mapImageSource: MapImageSource
        , @Value("#{jobParameters['request.id']}")id: String
        , @Value("#{jobParameters['input.file.name']}")inputFileName: String
        , @Value("#{jobParameters['output.file.name']}")name: String): Step {
        return stepBuilderFactory.get("mapping")
            .chunk<FieldSet, Path>(10)
            .reader(FlatFileItemReaderBuilder<FieldSet>()
                .name(id)
                .resource(FileSystemResource(inputFileName))
                .lineTokenizer(DelimitedLineTokenizer())
                .fieldSetMapper(PassThroughFieldSetMapper())
                .build())
            .processor(ItemProcessor {
                if (it.values[0] != GPRMC_HEADER) {
                    return@ItemProcessor null
                }
                val latLon = LatAndLon.fromDegreeAndMinute(it.values[4], it.values[3], it.values[6], it.values[5])
                val tmp = Files.createTempFile(workDir, "", suffix)
                mapImageSource.getMapImage(latLon.latitude, latLon.longitude).transferTo(tmp.outputStream())
                return@ItemProcessor tmp
            })
            .writer { items ->
                val digits = (items.size * FPS).toString().length
                ZipOutputStream(FileOutputStream(name)).use { zos ->
                    var count = 0
                    items.forEach {
                        for (i in 1..FPS) {
                            zos.putNextEntry(ZipEntry(String.format("%0${digits}d.${suffix}", count++)))
                            it.inputStream().transferTo(zos)
                            zos.closeEntry()
                        }

                        it.deleteIfExists()
                    }
                }
            }
            .build()
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