package io.github.edy4c7.locationmapper.domains.entities

data class Progress(
    val total: Int,
    val processed: Int,
    val completed: Boolean = false,
    val url: String? = null
)