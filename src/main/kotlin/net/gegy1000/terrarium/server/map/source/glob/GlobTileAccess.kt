package net.gegy1000.terrarium.server.map.source.glob

import net.gegy1000.terrarium.server.map.glob.GlobType
import net.gegy1000.terrarium.server.map.source.tiled.TiledDataAccess

class GlobTileAccess(private val data: ByteArray, private val offsetX: Int, private val offsetZ: Int, val width: Int, val height: Int) : TiledDataAccess {
    fun get(x: Int, z: Int) = GlobType[this.data[(x - this.offsetX) + (z - this.offsetZ) * width].toInt() and 0xFF]
}
