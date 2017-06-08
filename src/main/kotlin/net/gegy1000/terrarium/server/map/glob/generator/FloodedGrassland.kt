package net.gegy1000.terrarium.server.map.glob.generator

import net.gegy1000.terrarium.server.map.glob.GlobGenerator
import net.gegy1000.terrarium.server.map.glob.GlobType
import net.gegy1000.terrarium.server.map.glob.generator.layer.ReplaceRandomLayer
import net.gegy1000.terrarium.server.map.glob.generator.layer.SelectionSeedLayer
import net.minecraft.block.BlockDirt
import net.minecraft.block.BlockLiquid
import net.minecraft.block.BlockTallGrass
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.world.World
import net.minecraft.world.chunk.ChunkPrimer
import net.minecraft.world.gen.layer.GenLayer
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom
import java.util.Random

class FloodedGrassland : GlobGenerator(GlobType.FLOODED_GRASSLAND) {
    companion object {
        private const val LAYER_WATER = 0
        private const val LAYER_PODZOL = 1
        private const val LAYER_DIRT = 2

        private val WATER = Blocks.WATER.defaultState
        private val DIRT = Blocks.DIRT.defaultState.withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT)
        private val PODZOL = Blocks.DIRT.defaultState.withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL)

        private val TALL_GRASS = Blocks.TALLGRASS.defaultState.withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS)
        private val LILYPAD = Blocks.WATERLILY.defaultState
    }

    lateinit var coverSelector: GenLayer
    lateinit var grassSelector: GenLayer

    override fun createLayers(world: World) {
        super.createLayers(world)

        var cover: GenLayer = SelectionSeedLayer(2, 1)
        cover = GenLayerVoronoiZoom(1000, cover)
        cover = ReplaceRandomLayer(replace = LAYER_PODZOL, replacement = LAYER_DIRT, chance = 4, seed = 2000, parent = cover)
        cover = GenLayerFuzzyZoom(3000, cover)

        this.coverSelector = cover
        this.coverSelector.initWorldGenSeed(world.seed)

        var grass: GenLayer = SelectionSeedLayer(2, 3000)
        grass = GenLayerVoronoiZoom(1000, grass)
        grass = GenLayerFuzzyZoom(2000, grass)

        this.grassSelector = grass
        this.grassSelector.initWorldGenSeed(world.seed)
    }

    override fun coverDecorate(globBuffer: Array<GlobType>, heightBuffer: IntArray, primer: ChunkPrimer, random: Random, x: Int, z: Int) {
        val grassLayer = this.sampleChunk(this.grassSelector, x, z)

        this.foreach(globBuffer) { localX: Int, localZ: Int ->
            val bufferIndex = localX + localZ * 16

            if (grassLayer[bufferIndex] == 1 && random.nextInt(6) != 0) {
                val y = heightBuffer[bufferIndex]
                val state = primer.getBlockState(localX, y, localZ)
                if (state.block is BlockLiquid) {
                    if (random.nextInt(10) == 0) {
                        primer.setBlockState(localX, y + 1, localZ, FloodedGrassland.LILYPAD)
                    }
                } else {
                    primer.setBlockState(localX, y + 1, localZ, FloodedGrassland.TALL_GRASS)
                }
            }
        }
    }

    override fun getCover(glob: Array<GlobType>, cover: Array<IBlockState>, x: Int, z: Int, random: Random) {
        val coverLayer = this.sampleChunk(this.coverSelector, x, z)

        this.foreach(glob) { localX: Int, localZ: Int ->
            val index = localX + localZ * 16
            cover[index] = when (coverLayer[index]) {
                FloodedGrassland.LAYER_WATER -> FloodedGrassland.WATER
                FloodedGrassland.LAYER_PODZOL -> FloodedGrassland.PODZOL
                else -> FloodedGrassland.DIRT
            }
        }
    }
}
