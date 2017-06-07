package net.gegy1000.terrarium.server.map.glob.generator

import net.gegy1000.terrarium.server.map.glob.GlobGenerator
import net.gegy1000.terrarium.server.map.glob.GlobType
import net.gegy1000.terrarium.server.map.glob.generator.layer.ConnectHorizontalLayer
import net.gegy1000.terrarium.server.map.glob.generator.layer.OutlineEdgeLayer
import net.gegy1000.terrarium.server.map.glob.generator.layer.SelectionSeedLayer
import net.minecraft.block.BlockCrops
import net.minecraft.block.BlockDirt
import net.minecraft.block.BlockFarmland
import net.minecraft.init.Blocks
import net.minecraft.world.World
import net.minecraft.world.chunk.ChunkPrimer
import net.minecraft.world.gen.layer.GenLayer
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom
import java.util.Random

open class Cropland(type: GlobType) : GlobGenerator(type) {
    companion object {
        const val LAYER_WHEAT = 0
        const val LAYER_CARROTS = 1
        const val LAYER_POTATOES = 2
        const val CROP_COUNT = 3

        const val LAYER_FENCE = 65535

        val WATER = Blocks.WATER.defaultState
        val FARMLAND = Blocks.FARMLAND.defaultState.withProperty(BlockFarmland.MOISTURE, 7)
        val COARSE_DIRT = Blocks.DIRT.defaultState.withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT)

        val WHEAT = Blocks.WHEAT.defaultState
        val CARROTS = Blocks.CARROTS.defaultState
        val POTATOES = Blocks.POTATOES.defaultState

        val FENCE = Blocks.OAK_FENCE.defaultState
    }

    lateinit var cropSelector: GenLayer

    override fun createLayers(world: World) {
        var layer: GenLayer = SelectionSeedLayer(Cropland.CROP_COUNT, 1)
        layer = GenLayerVoronoiZoom(1000, layer)
        layer = GenLayerFuzzyZoom(2000, layer)
        layer = GenLayerVoronoiZoom(3000, layer)
        layer = OutlineEdgeLayer(Cropland.LAYER_FENCE, 4000, layer)
        layer = ConnectHorizontalLayer(Cropland.LAYER_FENCE, 5000, layer)

        this.cropSelector = layer
        this.cropSelector.initWorldGenSeed(world.seed)
    }

    override fun coverDecorate(globBuffer: Array<GlobType>, heightBuffer: IntArray, primer: ChunkPrimer, random: Random, x: Int, z: Int) {
        val cropLayer = this.sampleChunk(this.cropSelector, x, z)

        this.foreach(globBuffer) { localX: Int, localZ: Int ->
            val age = random.nextInt(8)
            val bufferIndex = localX + localZ * 16

            val y = heightBuffer[bufferIndex]

            val state = when (cropLayer[bufferIndex]) {
                Cropland.LAYER_WHEAT -> Cropland.WHEAT
                Cropland.LAYER_CARROTS -> Cropland.CARROTS
                Cropland.LAYER_POTATOES -> Cropland.POTATOES
                else -> Cropland.FENCE
            }

            if (state.block is BlockCrops) {
                if (random.nextInt(20) != 0) {
                    if (primer.getBlockState(localX, y, localZ).block is BlockFarmland) {
                        primer.setBlockState(localX, y + 1, localZ, state.withProperty(BlockCrops.AGE, age))
                    }
                }
            } else {
                primer.setBlockState(localX, y, localZ, Cropland.COARSE_DIRT)
                primer.setBlockState(localX, y + 1, localZ, Cropland.FENCE)
            }
        }
    }

    override fun getCover(x: Int, z: Int, random: Random) = Cropland.FARMLAND
}
