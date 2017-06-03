package net.gegy1000.terrarium.server.map.source

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.server.map.source.GlobcoverSource.TILE_SIZE
import net.gegy1000.terrarium.server.world.Glob
import net.minecraft.util.math.MathHelper
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

object GlobcoverSource : ChunkMapperSource() {
    const val TILE_SIZE = 2560

    val GLOBCOVER_CACHE = File(CACHE_DIRECTORY, "globcover")

    private val cache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.SECONDS)
            .maximumSize(16)
            .build(object : CacheLoader<GlobTilePos, GlobTile>() {
                override fun load(pos: GlobTilePos): GlobTile {
                    return loadTile(pos) ?: GlobTile()
                }
            })

    operator fun get(x: Int, z: Int): Glob {
        val pos = GlobTilePos(MathHelper.intFloorDiv(x, TILE_SIZE), MathHelper.intFloorDiv(z, TILE_SIZE))
        val tile = this.getTile(pos)
        val localX = x - pos.minX
        val localZ = z - pos.minZ
        return tile[localX, localZ]
    }

    fun getTile(pos: GlobTilePos) = this.cache[pos]!!

    private fun loadTile(pos: GlobTilePos): GlobTile? {
        try {
            val cache = File(GLOBCOVER_CACHE, pos.name)
            if (cache.exists()) {
                return this.loadTile(pos, FileInputStream(cache), false)
            } else {
                val url = URL("$MAT/${pos.name}")
                return this.loadTile(pos, url.openStream(), true)
            }
        } catch (e: Exception) {
            Terrarium.LOGGER.warn("Failed to load Globcover tile: ${pos.name}", e)
        }
        return null
    }

    private fun loadTile(pos: GlobTilePos, input: InputStream, save: Boolean): GlobTile {
        val image = ImageIO.read(input)
        val buffer = (image.raster.dataBuffer as DataBufferByte).data

        if (save) {
            launch(CommonPool) { saveTile(pos, image) }
        }

        val offsetX = if (pos.tileX < 0) TILE_SIZE - image.width else 0
        val offsetZ = if (pos.tileZ < 0) TILE_SIZE - image.height else 0

        return GlobTile(buffer, offsetX, offsetZ, image.width, image.height)
    }

    private suspend fun saveTile(pos: GlobTilePos, image: BufferedImage) {
        if (!GLOBCOVER_CACHE.exists()) {
            GLOBCOVER_CACHE.mkdirs()
        }
        try {
            ImageIO.write(image, "png", File(GLOBCOVER_CACHE, pos.name))
        } catch (e: IOException) {
            Terrarium.LOGGER.error("Failed to save Globcover tile: ${pos.name}", e)
        }
    }
}

data class GlobTilePos(val tileX: Int, val tileZ: Int) {
    val name: String
        get() = "f_${tileX}_${tileZ}_.txt"

    val minX: Int
        get() = this.tileX * TILE_SIZE
    val minZ: Int
        get() = this.tileZ * TILE_SIZE
}

data class GlobTile(val data: ByteArray = ByteArray(TILE_SIZE * TILE_SIZE), val offsetX: Int = 0, val offsetZ: Int = 0, val width: Int = 0, val height: Int = 0) {
    operator fun get(x: Int, z: Int) = Glob[this.data[(x - this.offsetX) + (z - this.offsetZ) * TILE_SIZE].toInt() and 0xFF]
}
