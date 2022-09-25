package io.github.edy4c7.locationmapper.web.controllers

import io.github.edy4c7.locationmapper.domains.entities.MappingJob
import io.github.edy4c7.locationmapper.domains.services.JobLaunchingService
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.*
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletResponse

@RestController
@CrossOrigin(origins = ["http://localhost:3000"], methods = [GET, POST])
@RequestMapping("/mapping")
private class MappingController(private val service: JobLaunchingService) {
    @PostMapping
    fun post(nmea: MultipartFile, res: HttpServletResponse): MappingJob? {
        return service.launchJob(nmea.inputStream)
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): MappingJob? {
        return service.getJobProgress(id)
    }
}