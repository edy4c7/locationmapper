package io.github.edy4c7.locationmapper.web.controllers

import io.github.edy4c7.locationmapper.domains.dto.JobProgress
import io.github.edy4c7.locationmapper.domains.services.MappingService
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/mapping")
internal class MappingController(private val service: MappingService) {
    @PostMapping
    fun post(@RequestParam("nmea") nmea: MultipartFile, res: HttpServletResponse) : Map<String, String> {
        return mapOf("id" to service.requestProcess(nmea.inputStream))
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): JobProgress? {
        return service.getProgress(id)
    }
}