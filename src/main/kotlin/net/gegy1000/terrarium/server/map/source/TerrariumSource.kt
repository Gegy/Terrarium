package net.gegy1000.terrarium.server.map.source

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import net.gegy1000.terrarium.Terrarium
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL

open class TerrariumSource {
    companion object {
        private const val INFO_JSON = "https://gist.githubusercontent.com/gegy1000/0a0ac9ec610d6d9716d43820a0825a6d/raw/6c84cdd4cf566557b7671bab08684d0ff28351ae/terrarium_info.json"
        private val GSON = Gson()

        val CACHE_DIRECTORY = File(".", "mods/terrarium/cache/")

        lateinit var INFO: TerrariumSource.Info

        fun loadInfo() {
            val url = URL(INFO_JSON)
            val input = InputStreamReader(url.openStream())
            try {
                INFO = GSON.fromJson(input, Info::class.java)
            } catch (e: IOException) {
                Terrarium.LOGGER.error("Failed to load Terrarium info", e)
            } finally {
                input.close()
            }
        }
    }

    data class Info(
            @SerializedName("base_url")
            val baseURL: String,
            @SerializedName("glob_endpoint")
            val globEndpoint: String,
            @SerializedName("glob_query")
            val globQuery: String,
            @SerializedName("heights_endpoint")
            val heightsEndpoint: String,
            @SerializedName("heights_query")
            val heightsQuery: String,
            @SerializedName("height_tiles")
            val heightTiles: String
    )
}
