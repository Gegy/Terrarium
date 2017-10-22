package net.gegy1000.terrarium.server.map.source

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.server.map.glob.GlobType
import net.gegy1000.terrarium.server.util.Coordinate
import net.minecraft.util.math.MathHelper
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object GlobcoverSource : TerrariumSource() {
    const val TILE_SIZE = 2560

    val GLOBCOVER_CACHE = File(CACHE_DIRECTORY, "globcover")

    private val cache = CacheBuilder.newBuilder()
            .expireAfterAccess(4, TimeUnit.SECONDS)
            .maximumSize(3)
            .build(object : CacheLoader<TilePos, Tile>() {
                override fun load(pos: TilePos): Tile {
                    return loadTile(pos) ?: Tile()
                }
            })

    fun sampleArea(result: Array<GlobType>, minimumCoordinate: Coordinate, maximumCoordinate: Coordinate) {
        val size = maximumCoordinate - minimumCoordinate
        val width = size.globalX.toInt()
        val height = size.globalZ.toInt()
        if (result.size != width * height) {
            throw IllegalStateException("Expected result array of size $width*$height, got ${result.size}")
        }
        // TODO: Come back to more performant, but broken algorithm
        repeat(height) { y ->
            repeat(width) { x ->
                val globType = GlobcoverSource[minimumCoordinate.addGlobal(x.toDouble(), y.toDouble())]
                result[x + y * width] = globType
            }
        }
    }

    operator fun get(coordinate: Coordinate): GlobType {
        val pos = TilePos(MathHelper.floor(coordinate.globX / TILE_SIZE), MathHelper.floor(coordinate.globZ / TILE_SIZE))
        val tile = this.getTile(pos)
        val localX = MathHelper.floor(coordinate.globX) - pos.minX
        val localZ = MathHelper.floor(coordinate.globZ) - pos.minZ
        return tile[localX, localZ]
    }

    fun getTile(pos: TilePos) = this.cache[pos]!!

    private fun loadTile(pos: TilePos): Tile? {
        try {
            val cache = File(GLOBCOVER_CACHE, pos.name)
            if (cache.exists()) {
                return this.loadTile(pos, FileInputStream(cache))
            } else {
                val url = URL("${INFO.baseURL}/${INFO.globEndpoint}/${pos.name}")
                return this.loadTile(pos, url.openStream(), true)
            }
        } catch (e: Exception) {
            Terrarium.LOGGER.warn("Failed to load Globcover tile: ${pos.name}", e)
        }
        return null
    }

    private fun loadTile(pos: TilePos, input: InputStream, save: Boolean = false): Tile {
        val dataInput = DataInputStream(GZIPInputStream(input))
        val width = dataInput.readUnsignedShort()
        val height = dataInput.readUnsignedShort()
        val buffer = ByteArray(width * height)
        dataInput.readFully(buffer)

        if (save) {
            launch(CommonPool) { saveTile(pos, width, height, buffer) }
        }

        val offsetX = if (pos.tileX < 0) TILE_SIZE - width else 0
        val offsetZ = if (pos.tileZ < 0) TILE_SIZE - height else 0

        return Tile(buffer, offsetX, offsetZ, width, height)
    }

    private suspend fun saveTile(pos: TilePos, width: Int, height: Int, buffer: ByteArray) {
        if (!GLOBCOVER_CACHE.exists()) {
            GLOBCOVER_CACHE.mkdirs()
        }
        val output = DataOutputStream(GZIPOutputStream(FileOutputStream(File(GLOBCOVER_CACHE, pos.name))))
        try {
            output.writeShort(width)
            output.writeShort(height)
            output.write(buffer)
        } catch (e: IOException) {
            Terrarium.LOGGER.error("Failed to save Globcover tile: ${pos.name}", e)
        } finally {
            output.close()
        }
    }
    data class TilePos(val tileX: Int, val tileZ: Int) {

        val name: String
            get() = TerrariumSource.INFO.globQuery.format(tileX.toString(), tileZ.toString())
        val minX: Int
            get() = this.tileX * TILE_SIZE
        val minZ: Int
            get() = this.tileZ * TILE_SIZE

    }
    data class Tile(val data: ByteArray = ByteArray(TILE_SIZE * TILE_SIZE), val offsetX: Int = 0, val offsetZ: Int = 0, val width: Int = 0, val height: Int = 0) {
        operator fun get(x: Int, z: Int) = GlobType[this.data[(x - this.offsetX) + (z - this.offsetZ) * TILE_SIZE].toInt() and 0xFF]

    }
}
