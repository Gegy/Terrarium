package net.gegy1000.terrarium.server.world.generator

import net.gegy1000.terrarium.server.map.source.GlobcoverSource
import net.gegy1000.terrarium.server.map.source.HeightSource
import net.gegy1000.terrarium.server.util.Interpolation
import net.gegy1000.terrarium.server.world.EarthGenerationSettings
import net.gegy1000.terrarium.server.world.HeightProvider
import net.minecraft.util.math.MathHelper
import net.minecraft.world.biome.Biome

class EarthGenerationHandler(val settings: EarthGenerationSettings) : HeightProvider {
    companion object {
        const val WIDTH = 432000
        const val HEIGHT = 216000
        const val GLOB_WIDTH = 1313600
        const val GLOB_HEIGHT = 566800
        const val GLOB_SCALE_X = GLOB_WIDTH.toDouble() / WIDTH
        const val GLOB_SCALE_Y = GLOB_HEIGHT.toDouble() / HEIGHT
        const val REAL_SCALE = 92.766203
    }

    val buffer = Array(4, { DoubleArray(4) })

    val scale = this.settings.scale * REAL_SCALE
    val heightScale = this.settings.scale * this.settings.terrainHeightScale

    val oceanHeight: Int
        get() = 1

    override fun provideHeight(x: Int, z: Int): Int {
        val scaledWidth = (WIDTH * this.scale).toInt()
        val scaledHeight = (HEIGHT * this.scale).toInt()

        val scaledX = x.toDouble() / (scaledWidth - 1) * (WIDTH - 1)
        val scaledZ = z.toDouble() / (scaledHeight - 1) * (HEIGHT - 1)
        val originX = scaledX.toInt()
        val originZ = scaledZ.toInt()
        val intermediateX = scaledX - originX
        val intermediateZ = scaledZ - originZ

        for (u in 0..3) {
            for (v in 0..3) {
                this.buffer[u][v] = HeightSource[originX - 1 + u, originZ - 1 + v].toDouble()
            }
        }

        val bicubic = Interpolation.bicubic(this.buffer, intermediateX, intermediateZ)
        val scaled = (bicubic * this.heightScale * 2.0).toInt()

        if (bicubic >= 1.0 && scaled < 1) {
            return 1
        }

        return MathHelper.clamp(scaled, 0, 255)
    }

    fun provideBiome(x: Int, z: Int): Biome {
        val globScaledX = x * GLOB_SCALE_X
        val globScaledZ = z * GLOB_SCALE_Y

        val scaledWidth = (WIDTH * this.scale).toInt()
        val scaledHeight = (HEIGHT * this.scale).toInt()

        val scaledX = globScaledX / (scaledWidth - 1) * (WIDTH - 1)
        val scaledZ = globScaledZ / (scaledHeight - 1) * (HEIGHT - 1)

        val roundX = this.probRound(scaledX)
        val roundZ = this.probRound(scaledZ)
//
//        val roundX = scaledX.toInt()
//        val roundZ = scaledZ.toInt()

        return GlobcoverSource[roundX, roundZ].biome
        /*val scaledWidth = (WIDTH * this.scale).toInt()
        val scaledHeight = (HEIGHT * this.scale).toInt()

        if (x < 0 || z < 0 || x >= scaledWidth || z >= scaledHeight) {
            return DEFAULT_BIOME
        }

        val heightToBiome = HashMultimap.create<Int, Biome>()

        val buffer = Array(4) { DoubleArray(4) }

        val xScaled = x.toDouble() / (scaledWidth - 1) * (WIDTH - 1)
        val yScaled = z.toDouble() / (scaledHeight - 1) * (HEIGHT - 1)
        var xOrigin = xScaled.toInt()
        var yOrigin = yScaled.toInt()
        val xIntermediate = xScaled - xOrigin
        val yIntermediate = yScaled - yOrigin

        var prevBiome: Biome? = null
        var hasMultipleBiomes = false

        for (u in 0..3) {
            for (v in 0..3) {
                val dataX = xOrigin - 1 + u
                val dataY = yOrigin - 1 + v
                val blockHeight = HeightSource[dataX, dataY].toInt()
                buffer[u][v] = blockHeight.toDouble()
                val biome = this.getBiome(dataX, dataY)
                if (prevBiome != null && prevBiome != biome) {
                    hasMultipleBiomes = true
                }
                heightToBiome.put(blockHeight, biome)
                prevBiome = biome
            }
        }

        if (hasMultipleBiomes) {
            val interpolated = Interpolation.bicubic(buffer, xIntermediate, yIntermediate)

            var closestDistance = java.lang.Double.POSITIVE_INFINITY
            var closestHeight = 0

            for ((blockHeight) in heightToBiome.entries()) {
                val diff = Math.abs(blockHeight - interpolated)
                if (diff < closestDistance) {
                    closestHeight = blockHeight
                    closestDistance = diff
                }
            }

            val biomesForHeight = heightToBiome.get(closestHeight).toTypedArray()
            if (biomesForHeight.size != 1) {
                if (xIntermediate * xIntermediate + yIntermediate * yIntermediate + Random((xOrigin * yOrigin).toLong()).nextDouble() * 0.02 > 0.25) {
                    val phi = Math.atan2(yIntermediate, xIntermediate)
                    var dirPhi = (Math.floor((phi + Math.PI) / (2 * Math.PI) * 8.0 + 0.5) % 8.0).toInt()
                    if (dirPhi == 8) {
                        dirPhi = 7
                    }
                    if (dirPhi == 0 || dirPhi == 1 || dirPhi == 7) {
                        xOrigin -= 1
                    } else if (dirPhi == 3 || dirPhi == 4 || dirPhi == 5) {
                        xOrigin += 1
                    }
                    if (dirPhi == 1 || dirPhi == 2 || dirPhi == 3) {
                        yOrigin -= 1
                    } else if (dirPhi == 5 || dirPhi == 6 || dirPhi == 7) {
                        yOrigin += 1
                    }
                }
                return this.getBiome(xOrigin, yOrigin)
            } else {
                return biomesForHeight[0]
            }
        } else {
            return prevBiome
        }*/
    }

    private fun probRound(value: Double): Int {
        val floor = value.toInt()
        if (Math.random() > value - floor) {
            return floor
        } else {
            return floor + 1
        }
    }
}
