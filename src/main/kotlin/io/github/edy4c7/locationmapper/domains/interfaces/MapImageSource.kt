package io.github.edy4c7.locationmapper.domains.interfaces

import io.github.edy4c7.locationmapper.domains.valueobjects.Location
import java.io.InputStream

internal interface MapImageSource {
    fun getMapImage(location: Location): InputStream
}
