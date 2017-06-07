package net.gegy1000.terrarium.server.map.glob.generator

import net.gegy1000.terrarium.server.map.glob.GlobGenerator
import net.gegy1000.terrarium.server.map.glob.GlobType
import net.minecraft.block.BlockCrops
import net.minecraft.block.BlockDirt
import net.minecraft.block.BlockFarmland
import net.minecraft.init.Blocks
import net.minecraft.world.World
import net.minecraft.world.chunk.ChunkPrimer
import net.minecraft.world.gen.layer.GenLayer
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom
import net.minecraft.world.gen.layer.IntCache
import java.util.Random

open class Cropland(type: GlobType) : GlobGenerator(type) {
    companion object {
        const val LAYER_WHEAT = 0
        const val LAYER_CARROTS = 1
        const val LAYER_POTATOES = 2
        const val CROP_COUNT = 3

        val WATER = Blocks.WATER.defaultState
        val FARMLAND = Blocks.FARMLAND.defaultState.withProperty(BlockFarmland.MOISTURE, 7)
        val COARSE_DIRT = Blocks.DIRT.defaultState.withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT)

        val WHEAT = Blocks.WHEAT.defaultState
        val CARROTS = Blocks.CARROTS.defaultState
        val POTATOES = Blocks.POTATOES.defaultState
    }

    lateinit var cropSelector: GenLayer

    override fun createLayers(world: World) {
        var layer: GenLayer = SelectCropLayer(1)
        layer = GenLayerVoronoiZoom(1000, layer)
        layer = GenLayerFuzzyZoom(2000, layer)
        layer = GenLayerVoronoiZoom(5000, layer)

        this.cropSelector = layer
        this.cropSelector.initWorldGenSeed(world.seed)
    }

    override fun coverDecorate(globBuffer: Array<GlobType>, heightBuffer: IntArray, primer: ChunkPrimer, random: Random, x: Int, z: Int) {
        val cropLayer = this.sampleChunk(this.cropSelector, x, z)

        this.foreach(globBuffer) { localX: Int, localZ: Int ->
            if (random.nextInt(20) != 0) {
                val bufferIndex = localX + localZ * 16
                val y = heightBuffer[bufferIndex]

                if (primer.getBlockState(localX, y, localZ).block is BlockFarmland) {
                    val state = when (cropLayer[bufferIndex]) {
                        LAYER_WHEAT -> WHEAT
                        LAYER_CARROTS -> CARROTS
                        LAYER_POTATOES -> POTATOES
                        else -> WHEAT
                    }

                    primer.setBlockState(localX, y + 1, localZ, state.withProperty(BlockCrops.AGE, random.nextInt(8)))
                }
            }
        }
    }

    override fun getCover(x: Int, z: Int, random: Random) = FARMLAND
}

class SelectCropLayer(seed: Long) : GenLayer(seed) {
    override fun getInts(areaX: Int, areaY: Int, areaWidth: Int, areaHeight: Int): IntArray {
        val result = IntCache.getIntCache(areaWidth * areaHeight)
        for (z in 0..areaHeight - 1) {
            for (x in 0..areaWidth - 1) {
                this.initChunkSeed((areaX + x).toLong(), (areaY + z).toLong())
                result[x + z * areaWidth] = this.nextInt(Cropland.CROP_COUNT)
            }
        }
        return result
    }
}
