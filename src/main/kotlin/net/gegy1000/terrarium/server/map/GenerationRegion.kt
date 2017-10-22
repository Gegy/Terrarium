package net.gegy1000.terrarium.server.map

import net.gegy1000.terrarium.server.map.glob.GlobType
import net.gegy1000.terrarium.server.util.Coordinate
import net.minecraft.util.math.MathHelper

class GenerationRegion(
        val position: RegionTilePos,
        private val minimumCoordinate: Coordinate,
        private val scaledSize: Int,
        private val heights: ShortArray,
        private val globcover: Array<GlobType>
) {
    companion object {
        const val SIZE = 128
        const val SAMPLE_SIZE = SIZE + 1
    }

    fun getHeight(scaledX: Int, scaledZ: Int): Int {
        val localX = scaledX - MathHelper.floor(minimumCoordinate.blockX)
        val localZ = scaledZ - MathHelper.floor(minimumCoordinate.blockZ)
        return heights[localX + localZ * scaledSize].toInt()
    }

    fun getGlobType(scaledX: Int, scaledZ: Int): GlobType {
        val localX = scaledX - MathHelper.floor(minimumCoordinate.blockX)
        val localZ = scaledZ - MathHelper.floor(minimumCoordinate.blockZ)
        return globcover[localX + localZ * scaledSize]
    }
}
