package io.github.edy4c7.locationmapper.infrastructures

import io.github.edy4c7.locationmapper.domains.exceptions.SystemException
import io.github.edy4c7.locationmapper.domains.interfaces.MapImageSource
import io.github.edy4c7.locationmapper.domains.valueobjects.Location
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Component
@Profile("!test")
internal class MapQuestImageSource(
    private val httpClient: HttpClient,
    baseUri: String = "https://www.mapquestapi.com/staticmap/v5/map",
    @Value("\${mapquest.apikey}") apiKey: String,
) : MapImageSource {

    private val requestUri = "$baseUri?key=$apiKey"

    override fun getMapImage(location: Location): InputStream {
        val reqUrl = StringBuilder(requestUri)
            .append("&center=${location.latitude},${location.longitude}&")
            .append("format=png")

        val request = HttpRequest.newBuilder()
            .uri(URI.create(reqUrl.toString()))
            .build()

        val response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()).get()
        val statusClass = (response.statusCode()) / 100
        if (statusClass == 4 || statusClass == 5) {
            throw SystemException("MapQuest api returned status ${response.statusCode()}")
        }

        return response.body()
    }
}