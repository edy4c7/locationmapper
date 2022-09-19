package io.github.edy4c7.locationmapper.valueobjects

import io.github.edy4c7.locationmapper.domains.valueobjects.Location
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private class LocationTests {
    @Test
    fun testFromGprmcNE() {
        val actual = Location.fromGprmc(
            "\$GPRMC,044134.00,A,3514.97043,N,14000.09566,E,24.673,141.16,071121,,,D*5D")

        assertEquals(35.249507, actual.latitude, 2.6e-4)
        assertEquals(140.001594, actual.longitude, 2.6e-4)
    }

    @Test
    fun testFromGprmcSW() {
        val actual = Location.fromGprmc(
            "\$GPRMC,044134.00,A,3514.97043,S,14000.09566,W,24.673,141.16,071121,,,D*5D")

        assertEquals(-35.249507, actual.latitude, 2.6e-4)
        assertEquals(-140.001594, actual.longitude, 2.6e-4)
    }
}