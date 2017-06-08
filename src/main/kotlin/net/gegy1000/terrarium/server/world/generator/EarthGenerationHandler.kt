package net.gegy1000.terrarium.server.world.generator

import net.gegy1000.terrarium.server.map.glob.GlobType
import net.gegy1000.terrarium.server.map.source.GlobcoverSource
import net.gegy1000.terrarium.server.map.source.HeightSource
import net.gegy1000.terrarium.server.util.Interpolation
import net.gegy1000.terrarium.server.world.EarthGenerationSettings
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import java.util.Random

class EarthGenerationHandler(val world: World, val settings: EarthGenerationSettings) {
    companion object {
        const val WIDTH = 432000
        const val HEIGHT = 216000
        const val REAL_SCALE = 92.766203
    }

    val buffer = Array(4, { DoubleArray(4) })
    val random = Random(this.world.seed)

    val scale = this.settings.scale * REAL_SCALE
    val heightScale = this.settings.scale * this.settings.terrainHeightScale

    val scaledWidth = (WIDTH * this.scale).toInt()
    val scaledHeight = (HEIGHT * this.scale).toInt()

    val oceanHeight = this.settings.heightOffset + 1

    val scatterRange = (this.settings.scatterRange * this.settings.scale).toInt()

    fun getHeightRegion(buffer: IntArray, chunkX: Int, chunkZ: Int) {
        this.random.setSeed(chunkX.toLong() * 341873128712L + chunkZ.toLong() * 132897987541L)
        val x = chunkX shl 4
        val z = chunkZ shl 4
        for (localZ in 0..15) {
            for (localX in 0..15) {
                buffer[localX + localZ * 16] = this.getHeight(localX + x, localZ + z)
            }
        }
    }

    private fun getHeight(x: Int, z: Int): Int {
        val scaledX = x.toDouble() / (this.scaledWidth - 1) * (WIDTH - 1)
        val scaledZ = z.toDouble() / (this.scaledHeight - 1) * (HEIGHT - 1)
        val originX = MathHelper.floor(scaledX)
        val originZ = MathHelper.floor(scaledZ)
        val intermediateX = scaledX - originX
        val intermediateZ = scaledZ - originZ

        for (u in 0..3) {
            for (v in 0..3) {
                this.buffer[u][v] = HeightSource[originX - 1 + u, originZ - 1 + v].toDouble()
            }
        }

        val bicubic = Interpolation.bicubic(this.buffer, intermediateX, intermediateZ)
        val scaled = (bicubic * this.heightScale).toInt()

        if (bicubic >= 0.0 && scaled < 1) {
            return this.settings.heightOffset + 1
        }

        return MathHelper.clamp(scaled + this.settings.heightOffset, 0, 255)
    }

    fun getGlobRegion(buffer: Array<GlobType>, chunkX: Int, chunkZ: Int) {
        this.random.setSeed(chunkX.toLong() * 341873128712L + chunkZ.toLong() * 132897987541L)
        val x = chunkX shl 4
        val z = chunkZ shl 4
        for (localZ in 0..15) {
            for (localX in 0..15) {
                buffer[localX + localZ * 16] = this.getGlob(localX + x, localZ + z)
            }
        }
    }

    private fun getGlob(x: Int, z: Int): GlobType {
        val scatterX = x + this.random.nextInt(this.scatterRange) - this.random.nextInt(this.scatterRange)
        val scatterZ = z + this.random.nextInt(this.scatterRange) - this.random.nextInt(this.scatterRange)

        val scaledX = scatterX.toDouble() / (this.scaledWidth - 1) * (WIDTH - 1)
        val scaledZ = scatterZ.toDouble() / (this.scaledHeight - 1) * (HEIGHT - 1)

        val roundX = MathHelper.floor(scaledX * 3.0 / 10.0)
        val roundZ = MathHelper.floor(scaledZ * 3.0 / 10.0)

        return GlobcoverSource[roundX, roundZ]
    }
}
