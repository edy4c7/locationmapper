package io.github.edy4c7.locationmapper.web.controllers

import io.github.edy4c7.locationmapper.domains.services.MappingService
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletResponse

@Controller
@RequestMapping("/map")
internal class MapController(private val service: MappingService) {
    @PostMapping
    fun post(@RequestParam("nmea") nmea: MultipartFile, res: HttpServletResponse) {
        res.contentType = "application/zip"
        res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${nmea.originalFilename}\".zip")
        service.map(nmea.inputStream, res.outputStream)
    }
}