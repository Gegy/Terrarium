package net.gegy1000.terrarium.server.map.source

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.server.util.Coordinate
import net.minecraft.util.math.MathHelper
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.client.entity.GzipDecompressingEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object OverpassSource {
    const val SIZE_DEGREES = 0.2

    const val OVERPASS_ENDPOINT = "http://www.overpass-api.de/api/interpreter"
    const val QUERY_LOCATION = "/assets/terrarium/query/overpass_query.oql"

    const val QUERY_VERSION = 1

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
            .build(object : CacheLoader<TilePos, Tile>() {
                override fun load(pos: TilePos): Tile {
                    return OverpassSource.loadTile(pos) ?: Tile()
                }
            })

    fun loadQuery() {
        val input = InputStreamReader(OverpassSource::class.java.getResourceAsStream(QUERY_LOCATION))
        this.query = input.use { it.readText().replace("\n", "") }
    }

    fun sampleArea(minCoordinate: Coordinate, maxCoordinate: Coordinate): Tile {
        val minTilePos = this.getTilePos(minCoordinate)
        val maxTilePos = this.getTilePos(maxCoordinate)

        val elements = HashSet<Element>()

        for (tileZ in minTilePos.tileZ..maxTilePos.tileZ) {
            for (tileX in minTilePos.tileX..maxTilePos.tileX) {
                elements.addAll(this.getTile(TilePos(tileX, tileZ)).elements)
            }
        }

        return Tile(elements)
    }

    fun getTilePos(coordinate: Coordinate): TilePos {
        val tileX = MathHelper.floor(coordinate.longitude / OverpassSource.SIZE_DEGREES)
        val tileZ = MathHelper.ceil(-coordinate.latitude / OverpassSource.SIZE_DEGREES)
        return TilePos(tileX, tileZ)
    }

    fun getTile(pos: TilePos) = this.cache[pos]!!

    fun loadTile(pos: TilePos): Tile? {
        try {
            val cache = File(OSM_CACHE, pos.name)
            if (cache.exists()) {
                val metadata = File(OSM_CACHE, "${pos.name}.meta")
                if (metadata.exists()) {
                    var version = -1
                    val metadataInput = DataInputStream(FileInputStream(metadata))
                    metadataInput.use {
                        version = metadataInput.readUnsignedShort()
                    }
                    if (version != QUERY_VERSION) {
                        return this.requestTile(pos)
                    } else {
                        return this.loadTile(pos, GZIPInputStream(FileInputStream(cache)))
                    }
                }
            } else {
                return this.requestTile(pos)
            }
        } catch (e: IOException) {
            Terrarium.LOGGER.error("Failed to download overpass map tile ${pos.name}", e)
        }
        return null
    }

    private fun requestTile(pos: TilePos): Tile? {
        val post = HttpPost(OVERPASS_ENDPOINT)
        post.entity = StringEntity(this.query.format(pos.latitude, pos.longitude, pos.maxLatitude, pos.maxLongitude))

        val response = this.client.execute(post)
        if (response.statusLine.statusCode == 429) {
            Thread.sleep(150)
            response.close()
            return this.requestTile(pos)
        }

        return this.loadTile(pos, response.entity.content, true)
    }

    private fun loadTile(pos: TilePos, input: InputStream, save: Boolean = false): Tile? {
        try {
            val elements = HashSet<Element>()

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

            return Tile(elements)
        } catch (e: Exception) {
            Terrarium.LOGGER.error("Failed to download overpass map tile ${pos.name}", e)
        } finally {
            input.close()
        }

        return null
    }

    private fun saveTile(pos: TilePos, root: JsonObject) {
        if (!OSM_CACHE.exists()) {
            OSM_CACHE.mkdirs()
        }

        val cache = File(OSM_CACHE, pos.name)
        val metadata = File(OSM_CACHE, "${pos.name}.meta")

        val output = OutputStreamWriter(GZIPOutputStream(FileOutputStream(cache)))
        val metadataOutput = DataOutputStream(FileOutputStream(metadata))

        try {
            GSON.toJson(root, output)
            metadataOutput.writeShort(QUERY_VERSION)
        } catch (e: IOException) {
            Terrarium.LOGGER.error("Failed to save overpass map tile ${pos.name}}", e)
        } finally {
            output.close()
            metadataOutput.close()
        }
    }

    data class Element(val id: Int, val type: String, val latitude: Double, val longitude: Double, val nodes: List<Int>, val tags: Map<String, String>) {
        fun isType(key: String, value: String) = this.tags[key] == value

        override fun equals(other: Any?) = other is Element && other.id == id

        override fun hashCode() = id
    }

    class Tile(val elements: Set<Element> = hashSetOf()) {
        val nodes = Int2ObjectArrayMap<Element>()

        init {
            this.elements.filter { it.type == "node" }.forEach { this.nodes.put(it.id, it) }
        }
    }

    data class TilePos(val tileX: Int, val tileZ: Int) {
        val latitude: Double
            get() = -this.tileZ * OverpassSource.SIZE_DEGREES
        val longitude: Double
            get() = this.tileX * OverpassSource.SIZE_DEGREES

        val maxLatitude: Double
            get() = this.latitude + OverpassSource.SIZE_DEGREES
        val maxLongitude: Double
            get() = this.longitude + OverpassSource.SIZE_DEGREES

        val name: String
            get() = "${this.tileX}_${this.tileZ}.osm"
    }
}
