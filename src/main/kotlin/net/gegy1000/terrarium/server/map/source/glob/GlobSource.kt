package net.gegy1000.terrarium.server.map.source.glob

import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.server.map.glob.GlobType
import net.gegy1000.terrarium.server.map.source.CachedRemoteSource
import net.gegy1000.terrarium.server.map.source.TerrariumData
import net.gegy1000.terrarium.server.map.source.raster.RasterSource
import net.gegy1000.terrarium.server.map.source.tiled.DataTilePos
import net.gegy1000.terrarium.server.map.source.tiled.TiledSource
import net.gegy1000.terrarium.server.util.Coordinate
import net.gegy1000.terrarium.server.world.EarthGenerationSettings
import net.minecraft.util.math.MathHelper
import java.io.DataInputStream
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.zip.GZIPInputStream

class GlobSource(override val settings: EarthGenerationSettings) : TiledSource<GlobTileAccess>(TILE_SIZE, 4), RasterSource<GlobType>, CachedRemoteSource {
    companion object {
        const val TILE_SIZE = 2560
    }

    private val DataTilePos.minX
        get() = tileX * TILE_SIZE
    private val DataTilePos.minZ
        get() = tileY * TILE_SIZE

    override val defaultTile
            get() = GlobTileAccess(ByteArray(TILE_SIZE * TILE_SIZE), 0, 0, TILE_SIZE, TILE_SIZE)
    override val cacheRoot = File(CachedRemoteSource.globalCacheRoot, "globcover")

    override fun loadTile(pos: DataTilePos): GlobTileAccess? {
        val input = DataInputStream(getStream(pos))
        try {
            val width = input.readUnsignedShort()
            val height = input.readUnsignedShort()

            val offsetX = if (pos.tileX < 0) TILE_SIZE - width else 0
            val offsetZ = if (pos.tileY < 0) TILE_SIZE - height else 0

            val buffer = ByteArray(width * height)
            input.readFully(buffer)

            return GlobTileAccess(buffer, offsetX, offsetZ, width, height)
        } catch (e: IOException) {
            Terrarium.LOGGER.error("Failed to parse heights tile at $pos", e)
        } finally {
            input.close()
        }
        return null
    }

    override fun sampleArea(result: Array<GlobType>, minimumCoordinate: Coordinate, maximumCoordinate: Coordinate) {
        val size = maximumCoordinate - minimumCoordinate
        val width = size.globalX.toInt()
        val height = size.globalZ.toInt()
        if (result.size != width * height) {
            throw IllegalStateException("Expected result array of size $width*$height, got ${result.size}")
        }
        // TODO: Come back to more performant, but broken algorithm
        repeat(height) { y ->
            repeat(width) { x ->
                val globType = get(minimumCoordinate.addGlobal(x.toDouble(), y.toDouble()))
                result[x + y * width] = globType
            }
        }
    }

    override fun get(coordinate: Coordinate): GlobType {
        val pos = DataTilePos(MathHelper.floor(coordinate.globX / TILE_SIZE), MathHelper.floor(coordinate.globZ / TILE_SIZE))
        val tile = this.getTile(pos)
        return tile.get(MathHelper.floor(coordinate.globX) - pos.minX, MathHelper.floor(coordinate.globZ) - pos.minZ)
    }

    override fun getRemoteStream(key: DataTilePos) = GZIPInputStream(URL("${TerrariumData.INFO.baseURL}/${TerrariumData.INFO.globEndpoint}/${getCachedName(key)}").openStream())

    override fun getCachedName(key: DataTilePos) = TerrariumData.INFO.globQuery.format(key.tileX.toString(), key.tileY.toString())
}
