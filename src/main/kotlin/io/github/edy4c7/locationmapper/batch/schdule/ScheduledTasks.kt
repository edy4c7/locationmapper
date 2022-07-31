package io.github.edy4c7.locationmapper.batch.schdule

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.*

@Component
class ScheduledTasks(
    private val jobLauncher: JobLauncher,
    private val expiringJob: Job,
    @Value("\${fileRetentionPeriod}") private val fileRetentionPeriod: String,
) {
    @Scheduled(cron = "\${cron.expire}")
    fun expire() {
        val retentionDateTime = ZonedDateTime.now().minusHours(fileRetentionPeriod.toLong())
        jobLauncher.run(expiringJob,
            JobParametersBuilder().addDate("retentionDateTime",
                Date.from(retentionDateTime.toInstant())).toJobParameters())
    }
}