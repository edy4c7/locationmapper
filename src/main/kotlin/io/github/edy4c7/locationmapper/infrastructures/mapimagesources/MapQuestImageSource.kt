package io.github.edy4c7.locationmapper.infrastructures.mapimagesources

import io.github.edy4c7.locationmapper.domains.mapimagesources.MapImageSource
import io.github.edy4c7.locationmapper.domains.valueobjects.Location
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Component
internal class MapQuestImageSource(private val httpClient: HttpClient, @Value("\${mapquest.apikey}") apiKey: String) : MapImageSource {
    private val baseUri = "https://www.mapquestapi.com/staticmap/v5/map?key=$apiKey"

    override fun getMapImage(location: Location): InputStream {
        val reqUrl = StringBuilder(baseUri)
            .append("&center=${location.latitude},${location.longitude}&")
            .append("format=png")

        val request = HttpRequest.newBuilder()
            .uri(URI.create(reqUrl.toString()))
            .build()

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()).get().body()
    }
}