package io.github.edy4c7.locationmapper.web.controllers

import io.github.edy4c7.locationmapper.domains.services.BatchJobService
import org.springframework.batch.core.BatchStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/job")
internal class JobController(private val service: BatchJobService) {
    @PostMapping
    fun post(@RequestParam("nmea") nmea: MultipartFile, res: HttpServletResponse) : Map<String, String> {
        return mapOf("token" to service.requestProcess(nmea.inputStream))
    }

    @GetMapping("/{token}")
    fun get(@PathVariable token: String): BatchStatus? {
        return service.getProgress(token)
    }
}