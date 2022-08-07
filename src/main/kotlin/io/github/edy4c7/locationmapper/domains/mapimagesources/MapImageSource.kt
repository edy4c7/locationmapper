package io.github.edy4c7.locationmapper.domains.mapimagesources

import io.github.edy4c7.locationmapper.domains.exceptions.MapImageSourceException
import io.github.edy4c7.locationmapper.domains.valueobjects.Location
import java.io.InputStream

internal interface MapImageSource {
    @Throws(MapImageSourceException::class)
    fun getMapImage(location: Location): InputStream
}
