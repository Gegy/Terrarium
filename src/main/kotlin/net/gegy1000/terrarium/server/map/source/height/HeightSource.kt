package net.gegy1000.terrarium.server.map.source.height

import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.server.map.source.CachedRemoteSource
import net.gegy1000.terrarium.server.map.source.TerrariumData
import net.gegy1000.terrarium.server.map.source.raster.ShortRasterSource
import net.gegy1000.terrarium.server.map.source.tiled.DataTilePos
import net.gegy1000.terrarium.server.map.source.tiled.TiledSource
import net.gegy1000.terrarium.server.util.Coordinate
import net.gegy1000.terrarium.server.world.EarthGenerationSettings
import net.minecraft.util.math.MathHelper
import java.io.DataInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.zip.GZIPInputStream

class HeightSource(override val settings: EarthGenerationSettings) : TiledSource<HeightTileAccess>(TILE_SIZE, 8), ShortRasterSource, CachedRemoteSource {
    companion object {
        const val TILE_SIZE = 1201
        private val validTiles = HashSet<DataTilePos>()

        fun loadValidTiles() {
            val url = URL("${TerrariumData.INFO.baseURL}/${TerrariumData.INFO.heightsEndpoint}/${TerrariumData.INFO.heightTiles}")
            val input = DataInputStream(GZIPInputStream(url.openStream()))
            try {
                val count = input.readInt()
                for (i in 1..count) {
                    val latitude = input.readShort()
                    val longitude = input.readShort()
                    this.validTiles.add(DataTilePos(longitude.toInt(), latitude.toInt()))
                }
            } catch (e: IOException) {
                Terrarium.LOGGER.error("Failed to load valid Terrarium height tiles", e)
            } finally {
                input.close()
            }
        }
    }

    private val DataTilePos.minX
        get() = tileX * 1200
    private val DataTilePos.minZ
        get() = (-tileY - 1) * 1200

    override val defaultTile: HeightTileAccess
        get() = HeightTileAccess(ShortArray(1), 1, 1)
    override val cacheRoot = File(CachedRemoteSource.globalCacheRoot, "heights")

    override fun loadTile(pos: DataTilePos): HeightTileAccess? {
        if (validTiles.isEmpty() || validTiles.contains(pos)) {
            val input = DataInputStream(getStream(pos))
            try {
                val heightmap = ShortArray(TILE_SIZE * TILE_SIZE)
                for (index in 0 until heightmap.size) {
                    val height = input.readShort()
                    if (height >= 0) {
                        heightmap[index] = height
                    }
                }
                return HeightTileAccess(heightmap, TILE_SIZE, TILE_SIZE)
            } catch (e: IOException) {
                Terrarium.LOGGER.error("Failed to parse heights tile at $pos", e)
            } finally {
                input.close()
            }
        }
        return null
    }

    override fun get(coordinate: Coordinate): Short {
        val pos = DataTilePos(MathHelper.floor(coordinate.longitude), MathHelper.floor(coordinate.latitude))
        val tile = this.getTile(pos)
        return tile.get(MathHelper.floor(coordinate.globalX - pos.minX), MathHelper.floor(coordinate.globalZ - pos.minZ))
    }

    override fun sampleArea(result: ShortArray, minimumCoordinate: Coordinate, maximumCoordinate: Coordinate) {
        val size = maximumCoordinate - minimumCoordinate
        val width = size.globalX.toInt()
        val height = size.globalZ.toInt()
        if (result.size != width * height) {
            throw IllegalStateException("Expected result array of size ${width * height}, got ${result.size}")
        }
        /*val minimumX = MathHelper.floor(minimumCoordinate.globalX)
        val minimumZ = MathHelper.floor(minimumCoordinate.globalZ)
        val maximumX = MathHelper.floor(maximumCoordinate.globalX)
        val maximumZ = MathHelper.floor(maximumCoordinate.globalZ)
        for (tileLatitude in MathHelper.floor(minimumCoordinate.latitude)..MathHelper.floor(maximumCoordinate.latitude)) {
            for (tileLongitude in MathHelper.floor(minimumCoordinate.longitude)..MathHelper.floor(maximumCoordinate.longitude)) {
                val pos = DataTilePos(tileLongitude, tileLatitude)
                val minX = pos.tileX * 1200
                val minZ = (-pos.tileY - 1) * 1200
                val tile = this.getTile(pos)
                val minSampleZ = Math.max(0, minimumZ - minZ)
                val maxSampleZ = Math.min(1200, maximumZ - minZ)
                val minSampleX = Math.max(0, minimumX - minX)
                val maxSampleX = Math.min(1200, maximumX - minX)
                for (z in minSampleZ..maxSampleZ - 1) {
                    val globalZ = z + minZ
                    val resultZ = globalZ - minimumZ
                    val resultIndexZ = resultZ * width
                    for (x in minSampleX..maxSampleX - 1) {
                        val globalX = x + minX
                        val resultX = globalX - minimumX
                        result[resultX + resultIndexZ] = tile.get(x, z)
                    }
                }
            }
        }*/
        // TODO: Come back to more efficient, but broken algorithm
        repeat(height) { y ->
            repeat(width) { x ->
                val heightValue = get(minimumCoordinate.addGlobal(x.toDouble(), y.toDouble()))
                result[x + y * width] = heightValue
            }
        }
    }

    override fun getRemoteStream(key: DataTilePos): InputStream {
        val cachedName = getCachedName(key)
        val url = URL("${TerrariumData.INFO.baseURL}/${TerrariumData.INFO.heightsEndpoint}/$cachedName")
        return GZIPInputStream(url.openStream())
    }

    override fun getCachedName(key: DataTilePos): String {
        val latitudePrefix = if (key.tileY < 0) "S" else "N"
        val longitudePrefix = if (key.tileX < 0) "W" else "E"

        var latitudeString = Math.abs(key.tileY).toString()
        while (latitudeString.length < 2) {
            latitudeString = "0" + latitudeString
        }

        var longitudeString = Math.abs(key.tileX).toString()
        while (longitudeString.length < 3) {
            longitudeString = "0" + longitudeString
        }

        return TerrariumData.INFO.heightsQuery.format("$latitudePrefix$latitudeString", "$longitudePrefix$longitudeString")
    }
}
