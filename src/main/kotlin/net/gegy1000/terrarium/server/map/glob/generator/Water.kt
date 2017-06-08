package net.gegy1000.terrarium.server.map.glob.generator

import net.gegy1000.terrarium.server.map.glob.GlobGenerator
import net.gegy1000.terrarium.server.map.glob.GlobType
import net.gegy1000.terrarium.server.map.glob.generator.layer.SelectionSeedLayer
import net.minecraft.block.BlockDirt
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.world.World
import net.minecraft.world.gen.layer.GenLayer
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom
import net.minecraft.world.gen.layer.IntCache
import java.util.Random

class Water : GlobGenerator(GlobType.WATER) {
    companion object {
        private val WATER: IBlockState = Blocks.WATER.defaultState
        private val SAND: IBlockState = Blocks.SAND.defaultState
        private val DIRT: IBlockState = Blocks.DIRT.defaultState.withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT)
        private val CLAY: IBlockState = Blocks.CLAY.defaultState
        private val GRAVEL: IBlockState = Blocks.GRAVEL.defaultState
    }

    lateinit var coverSelector: GenLayer

    override fun createLayers(world: World) {
        super.createLayers(world)

        var layer: GenLayer = SelectionSeedLayer(2, 1)
        layer = GenLayerFuzzyZoom(1000, layer)
        layer = CoverLayer(2000, layer)
        layer = GenLayerVoronoiZoom(3000, layer)
        layer = GenLayerFuzzyZoom(4000, layer)

        this.coverSelector = layer
        this.coverSelector.initWorldGenSeed(world.seed)
    }

    override fun getCover(x: Int, z: Int, random: Random) = WATER

    override fun getFiller(glob: Array<GlobType>, filler: Array<IBlockState>, x: Int, z: Int, random: Random) {
        val coverLayer = this.sampleChunk(this.coverSelector, x, z)

        this.foreach(glob) { localX: Int, localZ: Int ->
            val index = localX + localZ * 16
            filler[index] = when (coverLayer[index]) {
                0 -> Water.SAND
                1 -> Water.GRAVEL
                2 -> Water.DIRT
                3 -> Water.CLAY
                else -> Water.DIRT
            }
        }
    }

    private class CoverLayer(seed: Long, parent: GenLayer) : GenLayer(seed) {
        init {
            this.parent = parent
        }

        override fun getInts(areaX: Int, areaY: Int, areaWidth: Int, areaHeight: Int): IntArray {
            val parent = this.parent.getInts(areaX, areaY, areaWidth, areaHeight)
            val result = IntCache.getIntCache(areaWidth * areaHeight)
            for (z in 0..areaHeight - 1) {
                for (x in 0..areaWidth - 1) {
                    this.initChunkSeed((areaX + x).toLong(), (areaY + z).toLong())
                    val sample = parent[x + z * areaWidth]
                    if (sample == 0) {
                        result[x + z * areaWidth] = this.nextInt(2)
                    } else {
                        result[x + z * areaWidth] = if (this.nextInt(20) == 0) 3 else 2
                    }
                }
            }
            return result
        }
    }
}
