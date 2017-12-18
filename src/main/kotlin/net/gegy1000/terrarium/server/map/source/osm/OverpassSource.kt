package net.gegy1000.terrarium.server.map.source.osm

import com.google.gson.Gson
import com.google.gson.JsonParser
import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.server.map.source.CachedRemoteSource
import net.gegy1000.terrarium.server.map.source.tiled.DataTilePos
import net.gegy1000.terrarium.server.map.source.tiled.TiledSource
import net.gegy1000.terrarium.server.util.Coordinate
import net.gegy1000.terrarium.server.world.EarthGenerationSettings
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

class OverpassSource(override val settings: EarthGenerationSettings) : TiledSource<OverpassTileAccess>(SIZE_DEGREES, 4), CachedRemoteSource {
    companion object {
        const val SIZE_DEGREES = 0.2

        const val OVERPASS_ENDPOINT = "http://www.overpass-api.de/api/interpreter"
        const val QUERY_LOCATION = "/assets/terrarium/query/overpass_query.oql"

        const val QUERY_VERSION = 1

        val GSON = Gson()
        val JSON_PARSER = JsonParser()

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
    }

    private val DataTilePos.latitude: Double
        get() = -tileY * OverpassSource.SIZE_DEGREES
    private val DataTilePos.longitude: Double
        get() = tileX * OverpassSource.SIZE_DEGREES

    private val DataTilePos.maxLatitude: Double
        get() = latitude + OverpassSource.SIZE_DEGREES
    private val DataTilePos.maxLongitude: Double
        get() = longitude + OverpassSource.SIZE_DEGREES

    lateinit var query: String

    override val defaultTile
        get() = OverpassTileAccess()
    override val cacheRoot = File(CachedRemoteSource.globalCacheRoot, "osm")

    fun loadQuery() {
        val input = InputStreamReader(OverpassSource::class.java.getResourceAsStream(QUERY_LOCATION))
        query = input.use { it.readText().replace("\n", "") }
    }

    fun getTilePos(coordinate: Coordinate): DataTilePos {
        val tileX = MathHelper.floor(coordinate.longitude / OverpassSource.SIZE_DEGREES)
        val tileZ = MathHelper.ceil(-coordinate.latitude / OverpassSource.SIZE_DEGREES)
        return DataTilePos(tileX, tileZ)
    }

    fun sampleArea(minCoordinate: Coordinate, maxCoordinate: Coordinate): OverpassTileAccess {
        val minTilePos = getTilePos(minCoordinate)
        val maxTilePos = getTilePos(maxCoordinate)

        val elements = hashSetOf<Element>()

        for (tileZ in minTilePos.tileY..maxTilePos.tileY) {
            for (tileX in minTilePos.tileX..maxTilePos.tileX) {
                elements.addAll(getTile(DataTilePos(tileX, tileZ)).elements)
            }
        }

        return OverpassTileAccess(elements)
    }

    override fun loadTile(pos: DataTilePos): OverpassTileAccess? {
        val input = getStream(pos)

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

            return OverpassTileAccess(elements)
        } catch (e: Exception) {
            Terrarium.LOGGER.error("Failed to download overpass map tile ${getCachedName(pos)}", e)
        } finally {
            input.close()
        }

        return null
    }

    override fun getRemoteStream(key: DataTilePos): InputStream {
        val post = HttpPost(OVERPASS_ENDPOINT)
        post.entity = StringEntity(query.format(key.latitude, key.longitude, key.maxLatitude, key.maxLongitude))

        val response = client.execute(post)
        if (response.statusLine.statusCode == 429) {
            // TODO: Handle rate limit better
            response.close()
            Thread.sleep(150)
            return getRemoteStream(key)
        }

        return response.entity.content
    }

    override fun getCachedName(key: DataTilePos) = "${key.tileX}_${key.tileY}.osm"

    override fun cacheMetadata(key: DataTilePos) {
        val metadataFile = File(cacheRoot, "${key.tileX}_${key.tileY}.meta")
        val metadataOutput = DataOutputStream(FileOutputStream(metadataFile))

        try {
            metadataOutput.writeShort(QUERY_VERSION)
        } catch (e: IOException) {
            Terrarium.LOGGER.error("Failed to cache OSM tile metadata at $key")
        } finally {
            metadataOutput.close()
        }
    }

    override fun shouldLoadCache(key: DataTilePos, file: File): Boolean {
        if (file.exists()) {
            val metadataFile = File(cacheRoot, "${key.tileX}_${key.tileY}.osm.meta")
            if (metadataFile.exists()) {
                val metadataInput = DataInputStream(FileInputStream(metadataFile).buffered())
                val version = metadataInput.use { metadataInput.readUnsignedShort() }
                return version == QUERY_VERSION
            }
        }
        return false
    }

    data class Element(val id: Int, val type: String, val latitude: Double, val longitude: Double, val nodes: List<Int>, val tags: Map<String, String>) {
        fun isType(key: String, value: String) = this.tags[key] == value

        override fun equals(other: Any?) = other is Element && other.id == id

        override fun hashCode() = id
    }
}
