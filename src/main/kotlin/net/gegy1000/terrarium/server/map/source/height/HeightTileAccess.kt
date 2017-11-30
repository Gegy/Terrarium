package net.gegy1000.terrarium.server.map.source.height

import net.gegy1000.terrarium.server.map.source.raster.RasterDataAccess

class HeightTileAccess(private val heightmap: ShortArray, override val width: Int, override val height: Int) : RasterDataAccess<Short> {
    override fun get(x: Int, y: Int) = heightmap[x + y * width]
}
