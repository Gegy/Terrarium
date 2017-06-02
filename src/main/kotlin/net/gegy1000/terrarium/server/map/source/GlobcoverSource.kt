package net.gegy1000.terrarium.server.map.source

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.server.map.source.GlobcoverSource.IMAGE_SIZE
import net.gegy1000.terrarium.server.map.source.GlobcoverSource.MAX_X
import net.gegy1000.terrarium.server.map.source.GlobcoverSource.MAX_Z
import net.gegy1000.terrarium.server.map.source.GlobcoverSource.MIN_X
import net.gegy1000.terrarium.server.map.source.GlobcoverSource.MIN_Z
import net.gegy1000.terrarium.server.map.source.GlobcoverSource.REGION_TILE
import net.gegy1000.terrarium.server.map.source.GlobcoverSource.getTile
import net.minecraft.init.Biomes
import net.minecraft.util.math.MathHelper
import net.minecraft.world.biome.Biome
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

fun main(args: Array<String>) {
    var minX = Int.MAX_VALUE
    var minZ = Int.MAX_VALUE
    var maxX = Int.MIN_VALUE
    var maxZ = Int.MIN_VALUE

    for (x in MIN_X..MAX_X) {
        for (z in MIN_Z..MAX_Z) {
            val pos = GlobTilePos(x, z)
            val tile = getTile(pos)

            val tileMinX = pos.minX / 10
            val tileMinZ = pos.minZ / 10
            val tileMaxX = tileMinX + tile.width
            val tileMaxZ = tileMinZ + tile.height

            if (tileMinX < minX) minX = tileMinX
            if (tileMinZ < minZ) minZ = tileMinZ
            if (tileMaxX > maxX) maxX = tileMaxX
            if (tileMaxZ > maxZ) maxZ = tileMaxZ
        }
    }

    println("$minX $minZ $maxX $maxZ - ${maxX - minX} ${maxZ - minZ}")
}

object GlobcoverSource : ChunkMapperSource() {
    const val MIN_X = -26 // 51 x 22
    const val MIN_Z = -13
    const val MAX_X = 25
    const val MAX_Z = 9
    const val IMAGE_SIZE = 2560
    const val REGION_TILE = 25600

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
        val pos = GlobTilePos(MathHelper.intFloorDiv(x, REGION_TILE), MathHelper.intFloorDiv(z, REGION_TILE))
        val tile = this.getTile(pos)
        val localX = (x - pos.minX) / 10
        val localZ = (z - pos.minZ) / 10
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

        val offsetX = if (pos.tileX < 0) IMAGE_SIZE - image.width else 0
        val offsetZ = if (pos.tileZ < 0) IMAGE_SIZE - image.height else 0

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
        get() = this.tileX * REGION_TILE
    val minZ: Int
        get() = this.tileZ * REGION_TILE
}

data class GlobTile(val data: ByteArray = ByteArray(IMAGE_SIZE * IMAGE_SIZE), val offsetX: Int = 0, val offsetZ: Int = 0, val width: Int = 0, val height: Int = 0) {
    operator fun get(x: Int, z: Int) = Glob[this.data[(x - this.offsetX) + (z - this.offsetZ) * IMAGE_SIZE].toInt() and 0xFF]
}

enum class Glob(val biome: Biome) {
    IRRIGATED_CROPS(Biomes.PLAINS),
    RAINFED_CROPS(Biomes.PLAINS),
    CROPLAND_WITH_VEGETATION(Biomes.PLAINS),
    VEGETATION_WITH_CROPLAND(Biomes.PLAINS),
    BROADLEAF_EVERGREEN(Biomes.FOREST),
    CLOSED_BROADLEAF_DECIDUOUS(Biomes.FOREST),
    OPEN_BROADLEAF_DECIDUOUS(Biomes.FOREST),
    CLOSED_NEEDLELEAF_EVERGREEN(Biomes.FOREST),
    OPEN_NEEDLELEAF(Biomes.FOREST),
    MIXED_BROAD_NEEDLELEAF(Biomes.FOREST),
    FOREST_SHRUBLAND_WITH_GRASS(Biomes.FOREST),
    GRASS_WITH_FOREST_SHRUBLAND(Biomes.PLAINS),
    SHRUBLAND(Biomes.DESERT),
    GRASSLAND(Biomes.PLAINS),
    SPARSE_VEGETATION(Biomes.DESERT),
    FRESH_FLOODED_FOREST(Biomes.SWAMPLAND),
    SALINE_FLOODED_FOREST(Biomes.SWAMPLAND),
    FLOODED_GRASSLAND(Biomes.SWAMPLAND),
    URBAN(Biomes.PLAINS),
    BARE(Biomes.DESERT),
    WATER(Biomes.RIVER),
    SNOW(Biomes.ICE_PLAINS),
    NO_DATA(Biomes.PLAINS);

    companion object {
        operator fun get(i: Int): Glob {
            return when (i) {
                11 -> IRRIGATED_CROPS
                14 -> RAINFED_CROPS
                20 -> CROPLAND_WITH_VEGETATION
                30 -> VEGETATION_WITH_CROPLAND
                40 -> BROADLEAF_EVERGREEN
                50 -> CLOSED_BROADLEAF_DECIDUOUS
                60 -> OPEN_BROADLEAF_DECIDUOUS
                70 -> CLOSED_NEEDLELEAF_EVERGREEN
                90 -> OPEN_NEEDLELEAF
                100 -> MIXED_BROAD_NEEDLELEAF
                110 -> FOREST_SHRUBLAND_WITH_GRASS
                120 -> GRASS_WITH_FOREST_SHRUBLAND
                130 -> SHRUBLAND
                140 -> GRASSLAND
                150 -> SPARSE_VEGETATION
                160 -> FRESH_FLOODED_FOREST
                170 -> SALINE_FLOODED_FOREST
                180 -> FLOODED_GRASSLAND
                190 -> URBAN
                200 -> BARE
                210 -> WATER
                220 -> SNOW
                else -> NO_DATA
            }
        }
    }
}