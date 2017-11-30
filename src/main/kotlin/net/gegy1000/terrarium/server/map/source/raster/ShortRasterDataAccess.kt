package net.gegy1000.terrarium.server.map.source.raster

import net.gegy1000.terrarium.server.map.source.tiled.TiledDataAccess

interface ShortRasterDataAccess : TiledDataAccess {
    val width: Int
    val height: Int

    fun get(x: Int, y: Int): Short
}
