package io.github.edy4c7.locationmapper.batch.schdule

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
private class ScheduledTasks(
    private val jobLauncher: JobLauncher,
    private val jobExplorer: JobExplorer,
    private val expiringJob: Job,
) {
    @Scheduled(cron = "\${cron.expire}")
    fun expire() {
        jobLauncher.run(expiringJob,
            JobParametersBuilder(jobExplorer).getNextJobParameters(expiringJob).toJobParameters())
    }
}