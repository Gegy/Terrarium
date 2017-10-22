package net.gegy1000.terrarium.server.util

import net.gegy1000.terrarium.server.world.EarthGenerationSettings
import net.minecraft.util.math.BlockPos

class Coordinate(private val settings: EarthGenerationSettings, val globalX: Double, val globalZ: Double) {
    companion object {
        fun fromLatLng(settings: EarthGenerationSettings, latitude: Double, longitude: Double): Coordinate {
            return Coordinate(settings, longitude * 1200.0, -latitude * 1200.0)
        }
    }

    val latitude: Double
        get() = -globalZ / 1200.0
    val longitude: Double
        get() = globalX / 1200.0

    val globX: Double
        get() = globalX * 3.0 / 10.0
    val globZ: Double
        get() = globalZ * 3.0 / 10.0

    val blockX: Double
        get() = globalX / settings.scaleRatioX
    val blockZ: Double
        get() = globalZ / settings.scaleRatioZ

    fun addGlobal(x: Double = 0.0, z: Double = 0.0) = Coordinate(settings, globalX + x, globalZ + z)

    operator fun plus(coordinate: Coordinate) = Coordinate(settings, globalX + coordinate.globalX, globalZ + coordinate.globalZ)

    operator fun minus(coordinate: Coordinate) = Coordinate(settings, globalX - coordinate.globalX, globalZ - coordinate.globalZ)

    fun toBlockPos(y: Double = 0.0) = BlockPos(blockX, y, blockZ)
}
