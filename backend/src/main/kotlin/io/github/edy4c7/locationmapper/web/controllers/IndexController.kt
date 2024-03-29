package io.github.edy4c7.locationmapper.web.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/")
private class IndexController {
    @GetMapping
    fun index(): String {
        return "index"
    }
}