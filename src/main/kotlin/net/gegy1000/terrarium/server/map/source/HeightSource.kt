package net.gegy1000.terrarium.server.map.source

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.server.util.Coordinate
import net.minecraft.util.math.MathHelper
import org.apache.commons.io.IOUtils
import java.io.ByteArrayInputStream
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
            .expireAfterAccess(8, TimeUnit.SECONDS)
            .maximumSize(16)
            .build(object : CacheLoader<TilePos, Tile>() {
                override fun load(pos: TilePos): Tile {
                    return loadTile(pos) ?: Tile()
                }
            })

    private val validTiles = HashSet<TilePos>()

    fun loadHeightPoints() {
        val url = URL("${INFO.baseURL}/${INFO.heightsEndpoint}/${INFO.heightTiles}")
        val input = DataInputStream(GZIPInputStream(url.openStream()))
        try {
            val count = input.readInt()
            for (i in 1..count) {
                val latitude = input.readShort().toInt()
                val longitude = input.readShort().toInt()
                this.validTiles.add(TilePos(latitude, longitude))
            }
        } catch (e: IOException) {
            Terrarium.LOGGER.error("Failed to load Terrarium height tiles", e)
        } finally {
            input.close()
        }
    }

    fun sampleArea(result: ShortArray, minimumCoordinate: Coordinate, maximumCoordinate: Coordinate) {
        val size = maximumCoordinate - minimumCoordinate
        val width = size.globalX.toInt()
        val height = size.globalZ.toInt()
        if (result.size != width * height) {
            throw IllegalStateException("Expected result array of size $width*$height, got ${result.size}")
        }
        /*val minimumX = MathHelper.floor(minimumCoordinate.globalX)
        val minimumZ = MathHelper.floor(minimumCoordinate.globalZ)
        val maximumX = MathHelper.floor(maximumCoordinate.globalX)
        val maximumZ = MathHelper.floor(maximumCoordinate.globalZ)
        for (tileLatitude in MathHelper.floor(minimumCoordinate.latitude)..MathHelper.floor(maximumCoordinate.latitude)) {
            for (tileLongitude in MathHelper.floor(minimumCoordinate.longitude)..MathHelper.floor(maximumCoordinate.longitude)) {
                val pos = TilePos(tileLatitude, tileLongitude)
                val tile = this.getTile(pos)
                val minSampleZ = Math.max(0, minimumZ - pos.minZ)
                val maxSampleZ = Math.min(1200, maximumZ - pos.minZ)
                val minSampleX = Math.max(0, minimumX - pos.minX)
                val maxSampleX = Math.min(1200, maximumX - pos.minX)
                for (z in minSampleZ..maxSampleZ - 1) {
                    val globalZ = z + pos.minZ
                    val resultZ = globalZ - minimumZ
                    val resultIndexZ = resultZ * width
                    for (x in minSampleX..maxSampleX - 1) {
                        val globalX = x + pos.minX
                        val resultX = globalX - minimumX
                        result[resultX + resultIndexZ] = tile[x, z]
                    }
                }
            }
        }*/
        // TODO: Come back to more performant, but broken algorithm
        repeat(height) { y ->
            repeat(width) { x ->
                val heightValue = HeightSource[minimumCoordinate.addGlobal(x.toDouble(), y.toDouble())]
                result[x + y * width] = heightValue
            }
        }
        /*launch(CommonPool) {
            val output = File("output_test/${minimumCoordinate.globalX}.${minimumCoordinate.globalZ}.png")
            output.parentFile.mkdirs()
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            repeat(height) { y ->
                repeat(width) { x ->
                    val heightValue = (result[x + y * width] / 4096.0 * 255).toInt() and 0xFF
                    image.setRGB(x, y, (heightValue shl 16) or (heightValue shl 8) or heightValue)
                }
            }
            ImageIO.write(image, "png", output)
        }*/
    }

    operator fun get(coordinate: Coordinate): Short {
        val pos = TilePos(MathHelper.floor(coordinate.latitude), MathHelper.floor(coordinate.longitude))
        val tile = this.getTile(pos)
        val tileX = coordinate.globalX - pos.minX
        val tileZ = coordinate.globalZ - pos.minZ
        val height = tile[tileX.toInt(), tileZ.toInt()]
        return height
    }

    fun getTile(pos: TilePos) = this.cache[pos]!!

    private fun loadTile(pos: TilePos): Tile? {
        if (this.validTiles.isEmpty() || this.validTiles.contains(pos)) {
            try {
                val cache = File(HEIGHT_CACHE, pos.name)
                if (cache.exists()) {
                    return Tile(this.loadTile(pos, FileInputStream(cache)))
                } else {
                    val url = URL("${INFO.baseURL}/${INFO.heightsEndpoint}/${pos.name}")
                    return Tile(this.loadTile(pos, url.openStream(), true))
                }
            } catch (e: IOException) {
                Terrarium.LOGGER.error("Failed to load Terrarium heights tile: ${pos.name}", e)
            }
        }
        return null
    }

    private fun loadTile(pos: TilePos, input: InputStream, save: Boolean = false): ShortArray {
        val heightmap = ShortArray(TILE_SIZE * TILE_SIZE)

        val raw = IOUtils.toByteArray(GZIPInputStream(input))
        val data = DataInputStream(ByteArrayInputStream(raw))
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

    data class TilePos(val latitude: Int, val longitude: Int) {
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
        val maxX: Int
            get() = minX + 1200
        val maxZ: Int
            get() = minZ + 1200

        override fun toString(): String {
            return "${this.latitude}_${this.longitude}"
        }
    }

    data class Tile(val heights: ShortArray = ShortArray(HeightSource.TILE_SIZE * HeightSource.TILE_SIZE)) {
        operator fun get(x: Int, z: Int) = this.heights[x + z * HeightSource.TILE_SIZE]
    }
}
