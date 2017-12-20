package net.gegy1000.terrarium.server.map.source

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.server.util.Coordinate
import net.gegy1000.terrarium.server.world.EarthGenerationSettings
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection

class GeocodingSource(override val settings: EarthGenerationSettings) : DataSource {
    private val JSON_PARSER = JsonParser()

    fun get(place: String): Coordinate? {
        val request = "https://maps.googleapis.com/maps/api/geocode/json?address=${URLEncoder.encode(place, "UTF-8")}"

        val url = URL(request)
        val connection = url.openConnection() as HttpsURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", Terrarium.MODID)

        val input = connection.inputStream

        try {
            val root = JSON_PARSER.parse(InputStreamReader(input)) as JsonObject

            if (root.has("results")) {
                val results = root["results"].asJsonArray

                results.map { it.asJsonObject }.filter { it.has("geometry") }.forEach { result ->
                    val geometry = result["geometry"].asJsonObject
                    val location = geometry["location"].asJsonObject

                    return Coordinate.fromLatLng(settings, location["lat"].asDouble, location["lng"].asDouble)
                }
            }
        } catch (e: IOException) {
            Terrarium.LOGGER.error("Failed to retrieve geocode for $place", e)
        } finally {
            input.close()
        }

        return null
    }
}
