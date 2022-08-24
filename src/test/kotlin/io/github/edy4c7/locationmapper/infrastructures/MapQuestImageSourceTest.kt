package io.github.edy4c7.locationmapper.infrastructures

import io.github.edy4c7.locationmapper.domains.exceptions.SystemException
import io.github.edy4c7.locationmapper.domains.valueobjects.Location
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.util.*
import java.util.concurrent.CompletableFuture

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MapQuestImageSourceTest {
    companion object {
        const val BASE_URL = "https://www.mapquestapi.com/staticmap/v5/map"
        const val API_KEY = "API_KEY_123456"
        const val LATITUDE = 12.34
        const val LONGITUDE = 56.78
    }

    private lateinit var httpClient: HttpClient

    private lateinit var mapQuestImageSource: MapQuestImageSource

    @BeforeAll
    fun beforeAll() {
        httpClient = mockk(relaxed = true)
        mapQuestImageSource = MapQuestImageSource(httpClient, API_KEY)

        mockkStatic(Files::class, UUID::class)
    }

    @Test
    fun successGetMapImage() {
        val response = mockk<HttpResponse<InputStream>>(relaxed = true)
        every { response.statusCode() } returns 200
        every { response.body() } returns ByteArrayInputStream(byteArrayOf(0x12, 0x34))

        every { httpClient.sendAsync<InputStream>(any(), any()) } returns CompletableFuture.completedFuture(response)

        mapQuestImageSource.getMapImage(Location(LATITUDE, LONGITUDE))

        val uri = StringBuilder(BASE_URL)
            .append("?key=$API_KEY")
            .append("&center=$LATITUDE,$LONGITUDE")
            .append("&format=png")

        val request = HttpRequest.newBuilder()
            .uri(URI.create(uri.toString()))
            .GET()
            .build()

        verify { httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()) }
    }

    @Test
    fun failedGetMapImage400() {
        val response = mockk<HttpResponse<InputStream>>(relaxed = true)
        every { response.body() } returns ByteArrayInputStream(ByteArray(0))
        every { response.statusCode() } returns 400

        every { httpClient.sendAsync<InputStream>(any(), any()) } returns CompletableFuture.completedFuture(response)

        assertThrows<SystemException>("MapQuest api returned status 400") {
            mapQuestImageSource.getMapImage(Location(LATITUDE, LONGITUDE))
        }
    }

    @Test
    fun failedGetMapImage500() {
        val response = mockk<HttpResponse<InputStream>>(relaxed = true)
        every { response.body() } returns ByteArrayInputStream(ByteArray(0))
        every { response.statusCode() } returns 500

        every { httpClient.sendAsync<InputStream>(any(), any()) } returns CompletableFuture.completedFuture(response)

        assertThrows<SystemException>("MapQuest api returned status 500") {
            mapQuestImageSource.getMapImage(Location(LATITUDE, LONGITUDE))
        }
    }
}