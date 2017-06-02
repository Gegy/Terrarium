package net.gegy1000.terrarium.server.util

import net.gegy1000.terrarium.server.world.EarthGenerationSettings
import net.gegy1000.terrarium.server.world.generator.EarthGenerationHandler
import net.minecraft.util.math.BlockPos

object Coordinates {
    fun fromLatitude(latitude: Double, settings: EarthGenerationSettings): Double {
        return -latitude * 1200 * EarthGenerationHandler.REAL_SCALE * settings.scale
    }

    fun fromLongitude(longitude: Double, settings: EarthGenerationSettings): Double {
        return longitude * 1200 * EarthGenerationHandler.REAL_SCALE * settings.scale
    }

    fun toBlockPos(latitude: Double, longitude: Double, settings: EarthGenerationSettings): BlockPos {
        return BlockPos(fromLongitude(longitude, settings), 0.0, fromLatitude(latitude, settings))
    }
}

