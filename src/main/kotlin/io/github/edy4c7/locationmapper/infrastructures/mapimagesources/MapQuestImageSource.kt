package io.github.edy4c7.locationmapper.infrastructures.mapimagesources

import io.github.edy4c7.locationmapper.domains.mapimagesources.MapImageSource
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

internal class MapQuestImageSource(private val httpClient: HttpClient, apiKey: String) : MapImageSource {
    private val baseUri = "https://https://www.mapquestapi.com/staticmap/v5/map?key=$apiKey"

    override fun getMapImage(latitude: Double, longitude: Double, width: Int, height: Int, zoom: Int): InputStream {
        val reqUrl = StringBuilder(baseUri)
            .append("&center=$latitude,$longitude&")
            .append("format=png")
            .append("&size=$width,$height")
            .append("&zoom=$zoom")

        val request = HttpRequest.newBuilder()
            .uri(URI.create(reqUrl.toString()))
            .build()

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()).get().body()
    }
}