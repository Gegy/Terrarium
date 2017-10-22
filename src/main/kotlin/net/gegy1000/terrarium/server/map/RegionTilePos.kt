package net.gegy1000.terrarium.server.map

import net.gegy1000.terrarium.server.util.Coordinate
import net.gegy1000.terrarium.server.world.EarthGenerationSettings

data class RegionTilePos(val tileX: Int, val tileZ: Int) {
    val minX: Int
        get() = tileX * GenerationRegion.SIZE
    val minZ: Int
        get() = tileZ * GenerationRegion.SIZE

    fun getMinCoordinate(settings: EarthGenerationSettings): Coordinate {
        return Coordinate(settings, tileX.toDouble() * GenerationRegion.SIZE, tileZ.toDouble() * GenerationRegion.SIZE)
    }

    fun getMaxCoordinate(settings: EarthGenerationSettings): Coordinate {
        return Coordinate(settings, (tileX + 1.0) * GenerationRegion.SIZE, (tileZ + 1.0) * GenerationRegion.SIZE)
    }
}
