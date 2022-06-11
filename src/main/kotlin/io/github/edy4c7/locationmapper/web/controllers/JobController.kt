package io.github.edy4c7.locationmapper.web.controllers

import io.github.edy4c7.locationmapper.domains.services.MappingService
import org.springframework.batch.core.BatchStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/mapping")
internal class JobController(private val service: MappingService) {
    @PostMapping
    fun post(@RequestParam("nmea") nmea: MultipartFile, res: HttpServletResponse) : Map<String, String> {
        return mapOf("id" to service.requestProcess(nmea.inputStream))
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): BatchStatus? {
        return service.getProgress(id)
    }
}