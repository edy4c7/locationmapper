package io.github.edy4c7.locationmapper.infrastructures

import io.github.edy4c7.locationmapper.domains.valueobjects.Location
import io.github.edy4c7.locationmapper.infrastructures.mapimagesources.MapQuestImageSource
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture

internal class MapQuestImageSourceTest {
    companion object{
        const val BASE_URL = "https://https://www.mapquestapi.com/staticmap/v5/map"
        const val API_KEY = "API_KEY_123456"
        const val LATITUDE = 12.34
        const val LONGITUDE = 56.78
    }

    @Test
    fun successGetMapImage() {
        val response = mock<HttpResponse<InputStream>> {
            on { body() } doReturn ByteArrayInputStream(byteArrayOf(0x12, 0x34))
        }

        val httpClient = mock<HttpClient> {
            on { sendAsync<InputStream>(any(), any()) } doReturn CompletableFuture.completedFuture(response)
        }

        val mapQuestImageSource = MapQuestImageSource(httpClient, API_KEY)

        mapQuestImageSource.getMapImage(Location(LATITUDE, LONGITUDE))

        val uri = StringBuilder(BASE_URL)
            .append("?key=$API_KEY")
            .append("&center=$LATITUDE,$LONGITUDE")
            .append("&format=png")

        val request = HttpRequest.newBuilder()
            .uri(URI.create(uri.toString()))
            .GET()
            .build()

        verify(httpClient).sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
    }
}