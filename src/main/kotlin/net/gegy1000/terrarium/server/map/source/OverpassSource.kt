package net.gegy1000.terrarium.server.map.source

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import net.gegy1000.terrarium.Terrarium
import net.minecraft.util.math.MathHelper
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.client.entity.GzipDecompressingEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object OverpassSource {
    const val TILE_SIZE = 128
    const val SIZE_DEGREES = TILE_SIZE / 1200.0

    const val OVERPASS_ENDPOINT = "http://www.overpass-api.de/api/interpreter"
    const val QUERY_LOCATION = "/assets/terrarium/query/overpass_query.oql"

    val GSON = Gson()
    val JSON_PARSER = JsonParser()

    val OSM_CACHE = File(TerrariumSource.CACHE_DIRECTORY, "osm")

    val client: CloseableHttpClient = HttpClientBuilder.create()
            .addInterceptorFirst { request: HttpRequest, _ ->
                if (!request.containsHeader("Accept-Encoding")) {
                    request.addHeader("Accept-Encoding", "gzip")
                }
                if (!request.containsHeader("User-Agent")) {
                    request.addHeader("User-Agent", Terrarium.MODID)
                }
            }
            .addInterceptorFirst { response: HttpResponse, _ ->
                val entity = response.entity
                entity.contentEncoding.elements.forEach {
                    if (it.name.equals("gzip", ignoreCase = true)) {
                        response.entity = GzipDecompressingEntity(entity)
                        return@forEach
                    }
                }
            }
            .build()

    lateinit var query: String

    private val cache = CacheBuilder.newBuilder()
            .expireAfterAccess(4, TimeUnit.SECONDS)
            .maximumSize(4)
            .build(object : CacheLoader<OverpassTilePos, OverpassTile>() {
                override fun load(pos: OverpassTilePos): OverpassTile {
                    return OverpassSource.loadTile(pos) ?: OverpassTile()
                }
            })

    fun loadQuery() {
        val input = InputStreamReader(OverpassSource::class.java.getResourceAsStream(QUERY_LOCATION))
        this.query = input.use { it.readText().replace("\n", "") }
    }

    fun getTile(x: Int, z: Int): OverpassTile {
        val pos = OverpassTilePos(-z * OverpassSource.SIZE_DEGREES, x * OverpassSource.SIZE_DEGREES)
        return this.getTile(pos)
    }

    fun getTile(pos: OverpassTilePos) = this.cache[pos]!!

    fun loadTile(pos: OverpassTilePos): OverpassTile? {
        try {
            val cache = File(OSM_CACHE, pos.name)
            if (cache.exists()) {
                return this.loadTile(pos, GZIPInputStream(FileInputStream(cache)))
            } else {
                val post = HttpPost(OVERPASS_ENDPOINT)
                post.entity = StringEntity(this.query.format(pos.latitude, pos.longitude, pos.maxLatitude, pos.maxLongitude))

                val response = this.client.execute(post)

                return this.loadTile(pos, response.entity.content, true)
            }
        } catch (e: IOException) {
            Terrarium.LOGGER.error("Failed to download overpass map tile ${pos.name}")
        }
        return null
    }

    private fun loadTile(pos: OverpassTilePos, input: InputStream, save: Boolean = false): OverpassTile? {
        try {
            val elements = ArrayList<Element>()

            val root = JSON_PARSER.parse(InputStreamReader(input)).asJsonObject
            val elementsArray = root["elements"].asJsonArray

            elementsArray.asSequence().map { it.asJsonObject }.forEach {
                val tags = HashMap<String, String>()
                val nodes = ArrayList<Int>()

                val id = it["id"].asInt
                val type = it["type"].asString
                val latitude = it["lat"]?.asDouble ?: 0.0
                val longitude = it["lon"]?.asDouble ?: 0.0

                if (it.has("tags")) {
                    val tagsObject = it["tags"].asJsonObject
                    tagsObject.entrySet().asSequence().filter { it.value.isJsonPrimitive }.forEach {
                        tags.put(it.key, it.value.asString)
                    }
                }

                if (it.has("nodes")) {
                    nodes.addAll(it["nodes"].asJsonArray.map { it.asInt })
                }

                elements.add(Element(id, type, latitude, longitude, nodes, tags))
            }

            if (save) {
                launch(CommonPool) { saveTile(pos, root) }
            }

            return OverpassTile(elements)
        } catch (e: IOException) {
            Terrarium.LOGGER.error("Failed to download overpass map tile ${pos.name}")
        } finally {
            input.close()
        }

        return null
    }

    private fun saveTile(pos: OverpassTilePos, root: JsonObject) {
        if (!OSM_CACHE.exists()) {
            OSM_CACHE.mkdirs()
        }

        val cache = File(OSM_CACHE, pos.name)
        val output = OutputStreamWriter(GZIPOutputStream(FileOutputStream(cache)))

        try {
            GSON.toJson(root, FileWriter(cache))
        } catch (e: IOException) {
            Terrarium.LOGGER.error("Failed to save overpass map tile ${pos.name}}")
        } finally {
            output.close()
        }
    }

    data class Element(val id: Int, val type: String, val latitude: Double, val longitude: Double, val nodes: List<Int>, val tags: Map<String, String>)
}

class OverpassTile(val elements: List<OverpassSource.Element> = arrayListOf<OverpassSource.Element>())

data class OverpassTilePos(val latitude: Double, val longitude: Double) {
    val minX: Int
        get() = MathHelper.floor(this.longitude / OverpassSource.SIZE_DEGREES)
    val minZ: Int
        get() = MathHelper.ceil(-this.latitude / OverpassSource.SIZE_DEGREES)

    val maxLatitude: Double
        get() = this.latitude + OverpassSource.SIZE_DEGREES
    val maxLongitude: Double
        get() = this.longitude + OverpassSource.SIZE_DEGREES

    val name: String
        get() = "${this.minX}_${this.minZ}.osm"
}
