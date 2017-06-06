package net.gegy1000.terrarium.server.map.glob.generator

import net.gegy1000.terrarium.server.map.glob.GlobGenerator
import net.gegy1000.terrarium.server.map.glob.GlobType
import net.minecraft.block.BlockCrops
import net.minecraft.block.BlockDirt
import net.minecraft.block.BlockFarmland
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.gen.layer.GenLayer
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom
import net.minecraft.world.gen.layer.IntCache
import java.util.Random

class RainfedCrops : GlobGenerator(GlobType.RAINFED_CROPS) {
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

    val pos = BlockPos.MutableBlockPos()

    override fun createLayers(world: World) {
        var layer: GenLayer = SelectCropLayer(1)
        layer = GenLayerVoronoiZoom(1000, layer)
        layer = GenLayerFuzzyZoom(2000, layer)
        layer = GenLayerVoronoiZoom(5000, layer)

        this.cropSelector = layer
        this.cropSelector.initWorldGenSeed(world.seed)
    }

    override fun coverDecorate(buffer: Array<GlobType>, world: World, random: Random, x: Int, z: Int) {
        val cropLayer = this.sampleChunk(this.cropSelector, x, z)

        this.foreach(buffer) { localX: Int, localZ: Int ->
            if (random.nextInt(20) != 0) {
                this.pos.setPos(x + localX, 0, z + localZ)
                val ground = world.getTopSolidOrLiquidBlock(this.pos)

                if (WHEAT.block.canPlaceBlockAt(world, ground)) {
                    val state = when (cropLayer[localX + localZ * 16]) {
                        LAYER_WHEAT -> WHEAT
                        LAYER_CARROTS -> CARROTS
                        LAYER_POTATOES -> POTATOES
                        else -> WHEAT
                    }

                    world.setBlockState(ground, state.withProperty(BlockCrops.AGE, random.nextInt(8)))
                }
            }
        }
    }

    override fun getCover(x: Int, z: Int, random: Random): IBlockState {
        if (x % 8 == 0 && z % 8 == 0) {
            return WATER
        }
        if (random.nextInt(20) == 0) {
            return COARSE_DIRT
        }
        return FARMLAND
    }
}

class SelectCropLayer(seed: Long) : GenLayer(seed) {
    override fun getInts(areaX: Int, areaY: Int, areaWidth: Int, areaHeight: Int): IntArray {
        val result = IntCache.getIntCache(areaWidth * areaHeight)
        for (z in 0..areaHeight - 1) {
            for (x in 0..areaWidth - 1) {
                this.initChunkSeed((areaX + x).toLong(), (areaY + z).toLong())
                result[x + z * areaWidth] = this.nextInt(RainfedCrops.CROP_COUNT)
            }
        }
        return result
    }
}
