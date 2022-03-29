package io.github.edy4c7.locationmapper.domains.utils

internal fun toDegreeLatLng(nsew: String, latLng: String) : Double {
    val minuteIndex = latLng.indexOf(".") - 2
    val deg = latLng.substring(0, minuteIndex).toDouble()
    val minute = latLng.substring(minuteIndex).toDouble()

    return (if (nsew == "S" || nsew == "W") -1 else 1) * (deg + minute / 60)
}
