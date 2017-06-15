package net.gegy1000.terrarium.server.map.source

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import net.gegy1000.terrarium.Terrarium
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

object HeightSource : TerrariumSource() {
    const val TILE_SIZE = 1201

    val HEIGHT_CACHE = File(CACHE_DIRECTORY, "heights")

    private val cache = CacheBuilder.newBuilder()
            .expireAfterAccess(4, TimeUnit.SECONDS)
            .maximumSize(4)
            .build(object : CacheLoader<HeightTilePos, HeightTile>() {
                override fun load(pos: HeightTilePos): HeightTile {
                    return loadTile(pos) ?: HeightTile()
                }
            })

    private val validTiles = HashSet<HeightTilePos>()

    suspend fun loadHeightPoints() {
        val url = URL("${INFO.baseURL}/${INFO.heightsEndpoint}/${INFO.heightTiles}")
        val input = DataInputStream(GZIPInputStream(url.openStream()))
        try {
            val count = input.readInt()
            for (i in 1..count) {
                val latitude = input.readShort().toInt()
                val longitude = input.readShort().toInt()
                this.validTiles.add(HeightTilePos(latitude, longitude))
            }
        } catch (e: IOException) {
            Terrarium.LOGGER.error("Failed to load Terrarium height tiles", e)
        } finally {
            input.close()
        }
    }

    operator fun get(x: Int, z: Int): Short {
        val pos = HeightTilePos(MathHelper.floor(-z / 1200.0), MathHelper.floor(x / 1200.0))
        val tile = this.getTile(pos)
        val tileX = x - pos.minX
        val tileZ = z - pos.minZ
        return tile[tileX, tileZ]
    }

    fun getTile(pos: HeightTilePos) = this.cache[pos]!!

    private fun loadTile(pos: HeightTilePos): HeightTile? {
        if (this.validTiles.isEmpty() || this.validTiles.contains(pos)) {
            try {
                val cache = File(HEIGHT_CACHE, pos.name)
                if (cache.exists()) {
                    return HeightTile(this.loadTile(pos, FileInputStream(cache)))
                } else {
                    val url = URL("${INFO.baseURL}/${INFO.heightsEndpoint}/${pos.name}")
                    return HeightTile(this.loadTile(pos, url.openStream(), true))
                }
            } catch (e: IOException) {
                Terrarium.LOGGER.error("Failed to load Terrarium heights tile: ${pos.name}", e)
            }
        }
        return null
    }

    private fun loadTile(pos: HeightTilePos, input: InputStream, save: Boolean = false): ShortArray {
        val heightmap = ShortArray(TILE_SIZE * TILE_SIZE)

        val data = DataInputStream(GZIPInputStream(input))
        data.use {
            for (index in 0..heightmap.size - 1) {
                val height = data.readShort()
                if (height >= 0) {
                    heightmap[index] = height
                } else {
                    heightmap[index] = 0
                }
            }
        }

        if (save) {
            launch(CommonPool) { saveTile(pos.name, heightmap) }
        }

        return heightmap
    }

    private suspend fun saveTile(name: String, heightmap: ShortArray) {
        if (!HEIGHT_CACHE.exists()) {
            HEIGHT_CACHE.mkdirs()
        }
        val output = DataOutputStream(GZIPOutputStream(FileOutputStream(File(HEIGHT_CACHE, name))))
        try {
            heightmap.forEach {
                output.writeShort(it.toInt())
            }
        } catch (e: IOException) {
            Terrarium.LOGGER.error("Failed to save Terrarium heights tile: $name", e)
        } finally {
            output.close()
        }
    }
}

data class HeightTilePos(val latitude: Int, val longitude: Int) {
    val name: String
        get() {
            val latitudePrefix = if (this.latitude < 0) "S" else "N"
            val longitudePrefix = if (this.longitude < 0) "W" else "E"

            var latitudeString = Math.abs(this.latitude).toString()
            while (latitudeString.length < 2) {
                latitudeString = "0" + latitudeString
            }

            var longitudeString = Math.abs(this.longitude).toString()
            while (longitudeString.length < 3) {
                longitudeString = "0" + longitudeString
            }

            return TerrariumSource.INFO.heightsQuery.format("$latitudePrefix$latitudeString", "$longitudePrefix$longitudeString")
        }

    val minX: Int
        get() = this.longitude * 1200
    val minZ: Int
        get() = -(this.latitude + 1) * 1200

    override fun toString(): String {
        return "${this.latitude}_${this.longitude}"
    }
}

data class HeightTile(val heights: ShortArray = ShortArray(HeightSource.TILE_SIZE * HeightSource.TILE_SIZE)) {
    operator fun get(x: Int, z: Int) = this.heights[x + z * HeightSource.TILE_SIZE]
}
