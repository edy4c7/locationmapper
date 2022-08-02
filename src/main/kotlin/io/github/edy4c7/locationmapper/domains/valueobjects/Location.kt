package io.github.edy4c7.locationmapper.domains.valueobjects

data class Location constructor(
    val latitude: Double,
    val longitude: Double
) {
    companion object {
        fun fromGprmc(gprmc: String): Location {
            val values = gprmc.split(",")
            return Location(toDegree(values[4], values[3]), toDegree(values[6], values[5]))
        }

        private fun toDegree(nsew: String, latLng: String): Double {
            val minuteIndex = latLng.indexOf(".") - 2
            val deg = latLng.substring(0, minuteIndex).toDouble()
            val minute = latLng.substring(minuteIndex).toDouble()

            return (if (nsew == "S" || nsew == "W") -1 else 1) * (deg + minute / 60)
        }
    }
}