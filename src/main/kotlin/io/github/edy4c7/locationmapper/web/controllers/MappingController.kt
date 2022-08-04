package io.github.edy4c7.locationmapper.web.controllers

import io.github.edy4c7.locationmapper.domains.entities.BatchJobStatus
import io.github.edy4c7.locationmapper.domains.services.JobLaunchingService
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/mapping")
internal class MappingController(private val service: JobLaunchingService) {
    @PostMapping
    fun post(@RequestParam("nmea") nmea: MultipartFile, res: HttpServletResponse): Map<String, String> {
        return mapOf("id" to service.launchJob(nmea.inputStream))
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): BatchJobStatus? {
        return service.getJobProgress(id)
    }
}