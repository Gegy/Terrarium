package net.gegy1000.terrarium.server.world.generator

import net.gegy1000.terrarium.server.world.EarthGenerationSettings
import net.minecraft.util.math.BlockPos
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.BiomeCache
import net.minecraft.world.biome.BiomeProvider
import net.minecraft.world.gen.layer.IntCache
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.*

class EarthBiomeProvider(settings: EarthGenerationSettings) : BiomeProvider() {
    val biomeCache = BiomeCache(this)
    val spawnBiomes = ArrayList(allowedBiomes)
    val handler = EarthGenerationHandler(settings)

    override fun getBiomesToSpawnIn() = this.spawnBiomes

    override fun getBiome(pos: BlockPos, defaultBiome: Biome?): Biome = this.biomeCache.getBiome(pos.x, pos.z, defaultBiome)

    @SideOnly(Side.CLIENT)
    override fun getTemperatureAtHeight(biomeTemperature: Float, height: Int) = biomeTemperature

    override fun getBiomesForGeneration(biomes: Array<Biome?>?, x: Int, z: Int, width: Int, height: Int): Array<Biome?>? {
        var newBiomes = biomes
        IntCache.resetIntCache()
        if (newBiomes == null || newBiomes.size < width * height) {
            newBiomes = arrayOfNulls<Biome>(width * height)
        }
        var i = 0
        for (partZ in 0..height - 1) {
            for (partX in 0..width - 1) {
                newBiomes[i] = this.handler.provideBiome(partX + x, partZ + z)
                i++
            }
        }
        return newBiomes
    }

    override fun getBiomes(biomes: Array<Biome?>?, x: Int, z: Int, width: Int, length: Int, cache: Boolean): Array<Biome?> {
        var newBiomes = biomes
        IntCache.resetIntCache()
        if (newBiomes == null || newBiomes.size < width * length) {
            newBiomes = arrayOfNulls<Biome>(width * length)
        }
        if (cache && width == 16 && length == 16 && x and 15 == 0 && z and 15 == 0) {
            val cachedBiomes = this.biomeCache.getCachedBiomes(x, z)
            System.arraycopy(cachedBiomes, 0, newBiomes, 0, width * length)
            return newBiomes
        } else {
            var i = 0
            for (partZ in 0..length - 1) {
                for (partX in 0..width - 1) {
                    newBiomes[i] = this.handler.provideBiome(partX + x, partZ + z)
                    i++
                }
            }
            return newBiomes
        }
    }

    override fun areBiomesViable(x: Int, z: Int, radius: Int, allowed: List<Biome?>): Boolean {
        IntCache.resetIntCache()
        val minX = x - radius shr 2
        val minZ = z - radius shr 2
        val maxX = x + radius shr 2
        val maxZ = z + radius shr 2
        val width = maxX - minX + 1
        val length = maxZ - minZ + 1
        for (partZ in 0..length - 1) {
            for (partX in 0..width - 1) {
                val biome = this.handler.provideBiome(partX + x, partZ + z)
                if (!allowed.contains(biome)) {
                    return false
                }
            }
        }
        return true
    }

    override fun findBiomePosition(x: Int, z: Int, radius: Int, biomes: List<Biome>, random: Random): BlockPos? {
        IntCache.resetIntCache()
        val minX = x - radius shr 2
        val minZ = z - radius shr 2
        val maxX = x + radius shr 2
        val maxZ = z + radius shr 2
        val width = maxX - minX + 1
        val length = maxZ - minZ + 1
        var pos: BlockPos? = null
        var j2 = 0
        var i = 0
        for (partZ in 0..length - 1) {
            for (partX in 0..width - 1) {
                val chunkX = minX + i % width shl 2
                val chunkZ = minZ + i / width shl 2
                val biome = this.handler.provideBiome(partX + x, partZ + z)
                if (biomes.contains(biome) && (pos == null || random.nextInt(j2 + 1) == 0)) {
                    pos = BlockPos(chunkX, 0, chunkZ)
                    ++j2
                }
                i++
            }
        }
        return pos
    }

    override fun cleanupCache() = this.biomeCache.cleanupCache()
}