package io.github.edy4c7.locationmapper.domains.mapimagesources

import io.github.edy4c7.locationmapper.domains.exceptions.MapImageSourceException
import java.io.InputStream

internal interface MapImageSource {
    @Throws(MapImageSourceException::class)
    fun getMapImage(
        latitude: Double,
        longitude: Double
    ): InputStream
}
