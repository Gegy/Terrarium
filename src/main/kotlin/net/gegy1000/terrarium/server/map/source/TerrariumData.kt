package net.gegy1000.terrarium.server.map.source

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import net.gegy1000.terrarium.Terrarium
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.URL

object TerrariumData {
    private const val INFO_JSON = "https://gist.githubusercontent.com/gegy1000/0a0ac9ec610d6d9716d43820a0825a6d/raw/terrarium_info.json"
    private val GSON = GsonBuilder().setPrettyPrinting().create()

    val cacheRoot = File(".", "mods/terrarium/cache/")
    val infoCache = File(cacheRoot, "terrarium_info.json")

    var info = TerrariumData.Info("", "", "%s_%s.mat", "", "%s%s.hgt", "")

    fun loadInfo() {
        if (!cacheRoot.exists()) {
            cacheRoot.mkdirs()
        }

        val url = URL(INFO_JSON)
        val input = url.openStream()
        try {
            info = loadInfo(input)
            cacheInfo(info)
        } catch (e: IOException) {
            Terrarium.LOGGER.error("Failed to load remote Terrarium info, checking cache", e)
            loadCachedInfo()
        } finally {
            input.close()
        }
    }

    private fun loadCachedInfo() {
        val input = FileInputStream(infoCache)
        try {
            info = loadInfo(input)
        } catch (e: IOException) {
            Terrarium.LOGGER.error("Failed to load cached Terrarium info", e)
        } finally {
            input.close()
        }
    }

    private fun loadInfo(input: InputStream): TerrariumData.Info {
        val reader = InputStreamReader(input.buffered())
        return GSON.fromJson(reader, Info::class.java)
    }

    private fun cacheInfo(info: TerrariumData.Info) {
        val output = PrintWriter(FileOutputStream(infoCache))
        try {
            output.write(GSON.toJson(info))
        } catch (e: IOException) {
            Terrarium.LOGGER.error("Failed to cache Terrarium info", e)
        } finally {
            output.close()
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
