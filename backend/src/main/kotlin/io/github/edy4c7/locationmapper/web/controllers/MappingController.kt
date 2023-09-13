package io.github.edy4c7.locationmapper.web.controllers

import io.github.edy4c7.locationmapper.domains.entities.MappingJob
import io.github.edy4c7.locationmapper.domains.services.JobLaunchingService
import io.github.edy4c7.locationmapper.domains.services.MappingService
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/mapping")
private class MappingController(private val service: JobLaunchingService, private val mappingService: MappingService) {
    @PostMapping
    fun post(nmea: MultipartFile, res: HttpServletResponse): SseEmitter {
        val sse = SseEmitter(60 * 1000 * 30)
        mappingService.map(UUID.randomUUID().toString(), nmea.inputStream, sse)
        return sse
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): MappingJob? {
        return service.getJobProgress(id)
    }
}