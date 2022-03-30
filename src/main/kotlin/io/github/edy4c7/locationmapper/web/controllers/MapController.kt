package io.github.edy4c7.locationmapper.web.controllers

import io.github.edy4c7.locationmapper.domains.services.MappingService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartRequest
import java.io.OutputStream

@RestController("/map")
internal class MapController(private val service: MappingService) {
    @PostMapping
    fun get(mr: MultipartRequest, output: OutputStream) {
        val file = mr.fileMap.toList()[0].second
        service.map(file.inputStream, output)
    }
}