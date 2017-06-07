package net.gegy1000.terrarium.server.map.glob.generator

import net.gegy1000.terrarium.server.map.glob.GlobType
import net.minecraft.block.state.IBlockState
import net.minecraft.world.World
import net.minecraft.world.gen.layer.GenLayer
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom
import net.minecraft.world.gen.layer.IntCache
import java.util.Random

class RainfedCrops : Cropland(GlobType.RAINFED_CROPS) {
    lateinit var coverSelector: GenLayer

    override fun createLayers(world: World) {
        super.createLayers(world)

        var layer: GenLayer = CoverLayer(50)
        layer = GenLayerFuzzyZoom(5, layer)
        layer = GenLayerFuzzyZoom(2000, layer)

        this.coverSelector = layer
        this.coverSelector.initWorldGenSeed(world.seed)
    }

    override fun getCover(glob: Array<GlobType>, cover: Array<IBlockState>, x: Int, z: Int, random: Random) {
        val coverLayer = this.sampleChunk(this.coverSelector, x, z)

        this.foreach(glob) { localX: Int, localZ: Int ->
            val index = localX + localZ * 16
            if (random.nextInt(40) == 0) {
                cover[index] = WATER
            } else {
                cover[index] = when (coverLayer[index]) {
                    0 -> FARMLAND
                    1 -> WATER
                    else -> COARSE_DIRT
                }
            }
        }
    }

    private class CoverLayer(seed: Long) : GenLayer(seed) {
        override fun getInts(areaX: Int, areaY: Int, areaWidth: Int, areaHeight: Int): IntArray {
            val result = IntCache.getIntCache(areaWidth * areaHeight)
            for (z in 0..areaHeight - 1) {
                for (x in 0..areaWidth - 1) {
                    this.initChunkSeed((areaX + x).toLong(), (areaY + z).toLong())
                    val index = x + z * areaWidth
                    if (this.nextInt(10) == 0) {
                        if (this.nextInt(5) == 0) {
                            result[index] = 1
                        } else {
                            result[index] = 2
                        }
                    } else {
                        result[index] = 0
                    }
                }
            }
            return result
        }
    }
}
