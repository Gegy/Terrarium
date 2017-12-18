package net.gegy1000.terrarium.server.world.generator

import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.server.capability.TerrariumWorldData
import net.gegy1000.terrarium.server.map.GenerationRegionHandler
import net.gegy1000.terrarium.server.map.RegionTilePos
import net.gegy1000.terrarium.server.map.glob.GlobType
import net.gegy1000.terrarium.server.util.Interpolation
import net.gegy1000.terrarium.server.world.EarthGenerationSettings
import net.minecraft.util.math.MathHelper
import java.util.Random

class EarthGenerationHandler(worldData: TerrariumWorldData, val settings: EarthGenerationSettings, val maxHeight: Int) {
    companion object {
        const val DATA_WIDTH = 432000
        const val DATA_HEIGHT = 216000
        const val REAL_SCALE = 92.766203
    }

    val random = Random()

    val oceanHeight = this.settings.heightOffset + 1
    val scatterRange = (this.settings.scatterRange * this.settings.scale).toInt()

    val finalScale = this.settings.scale * REAL_SCALE
    val heightScale = this.settings.scale * this.settings.terrainHeightScale

    val regionHandler = GenerationRegionHandler(worldData, this)

    fun initializeSeed(pos: RegionTilePos) {
        random.setSeed(pos.tileX.toLong() * 341873128712L + pos.tileZ.toLong() * 132897987541L)
    }

    fun scaleHeightRegion(result: ShortArray, sample: ShortArray, width: Int, height: Int, scaledWidth: Int, scaledHeight: Int) {
        for (localZ in 0 until scaledHeight) {
            val scaledZ = localZ * settings.scaleRatioZ
            val originZ = MathHelper.floor(scaledZ)
            val intermediateZ = scaledZ - originZ

            for (localX in 0 until scaledWidth) {
                val scaledX = localX * settings.scaleRatioX
                val originX = MathHelper.floor(scaledX)
                val intermediateX = scaledX - originX

                val sampleIndex = originX + originZ * width
                val current = sample[sampleIndex].toDouble()
                val south = sample[sampleIndex + width].toDouble()
                val east = sample[sampleIndex + 1].toDouble()
                val southEast = sample[sampleIndex + width + 1].toDouble()

                val y1 = Interpolation.cosine(current, south, intermediateZ)
                val y2 = Interpolation.cosine(east, southEast, intermediateZ)

                val interpolated = Interpolation.cosine(y1, y2, intermediateX)
                val scaled = (interpolated * this.heightScale).toInt()

                val resultIndex = localX + localZ * scaledWidth
                if (interpolated >= 0.0 && scaled < 1) {
                    result[resultIndex] = (this.settings.heightOffset + 1).toShort()
                } else {
                    result[resultIndex] = MathHelper.clamp(scaled + this.settings.heightOffset, 0, this.maxHeight).toShort()
                }
            }
        }
    }

    fun scaleGlobRegion(result: Array<GlobType>, sample: Array<GlobType>, width: Int, height: Int, scaledWidth: Int, scaledHeight: Int) {
        for (localZ in 0 until scaledHeight) {
            val scaledZ = localZ * settings.scaleRatioZ
            val originZ = MathHelper.floor(scaledZ)

            for (localX in 0 until scaledWidth) {
                val scaledX = localX * settings.scaleRatioX
                val originX = MathHelper.floor(scaledX)

                result[localX + localZ * scaledWidth] = sample[originX + originZ * width]
            }
        }
    }

    fun populateHeightRegion(buffer: IntArray, chunkX: Int, chunkZ: Int) {
        try {
            val x = chunkX shl 4
            val z = chunkZ shl 4

            for (localZ in 0..15) {
                val blockZ = z + localZ
                val globalOriginZ = MathHelper.floor(blockZ * settings.scaleRatioZ)

                for (localX in 0..15) {
                    val blockX = x + localX
                    val globalOriginX = MathHelper.floor(blockX * settings.scaleRatioX)

                    val region = this.regionHandler[globalOriginX, globalOriginZ]
                    buffer[localX + localZ * 16] = region.getHeight(blockX, blockZ)
                }
            }
        } catch (e: Exception) {
            buffer.fill(5)
            Terrarium.LOGGER.error("Failed to generate heightmap for $chunkX, $chunkZ", e)
        }
    }

    fun populateGlobRegion(buffer: Array<GlobType>, chunkX: Int, chunkZ: Int) {
        try {
            val x = chunkX shl 4
            val z = chunkZ shl 4

            for (localZ in 0..15) {
                val blockZ = z + localZ
                for (localX in 0..15) {
                    val blockX = x + localX
                    buffer[localX + localZ * 16] = getGlobScattered(blockX, blockZ)
                }
            }
        } catch (e: Exception) {
            buffer.fill(GlobType.NO_DATA)
            Terrarium.LOGGER.error("Failed to generate globcover map for $chunkX, $chunkZ", e)
        }
    }

    private fun getGlobScattered(x: Int, z: Int): GlobType {
        val originGlob = this.getGlob(x, z)

        val range = MathHelper.ceil(this.scatterRange * originGlob.scatterScale)

        val scatterX = x + this.random.nextInt(range) - this.random.nextInt(range)
        val scatterZ = z + this.random.nextInt(range) - this.random.nextInt(range)

        val scattered = this.getGlob(scatterX, scatterZ)

        if (!scattered.scatterTo) {
            return originGlob
        }

        return scattered
    }

    private fun getGlob(x: Int, z: Int): GlobType {
        val globalOriginX = MathHelper.floor(x * settings.scaleRatioX)
        val globalOriginZ = MathHelper.floor(z * settings.scaleRatioZ)

        val region = this.regionHandler[globalOriginX, globalOriginZ]
        return region.getGlobType(x, z)
    }
}
