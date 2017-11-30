package net.gegy1000.terrarium.server.map.source.raster

import net.gegy1000.terrarium.server.map.source.tiled.TiledDataAccess

interface RasterDataAccess<out T> : TiledDataAccess {
    val width: Int
    val height: Int

    fun get(x: Int, y: Int): T
}
