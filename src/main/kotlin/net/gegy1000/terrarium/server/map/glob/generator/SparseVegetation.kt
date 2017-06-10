package net.gegy1000.terrarium.server.map.glob.generator

import net.gegy1000.terrarium.server.map.glob.GlobGenerator
import net.gegy1000.terrarium.server.map.glob.GlobType
import net.gegy1000.terrarium.server.map.glob.generator.layer.SelectionSeedLayer
import net.minecraft.block.BlockDirt
import net.minecraft.block.BlockTallGrass
import net.minecraft.init.Blocks
import net.minecraft.world.chunk.ChunkPrimer
import net.minecraft.world.gen.layer.GenLayer
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom
import java.util.Random

class SparseVegetation : GlobGenerator(GlobType.SPARSE_VEGETATION) {
    companion object {
        private const val LAYER_DIRT = 0
        private const val LAYER_SAND = 1

        private val DIRT = Blocks.DIRT.defaultState.withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT)
        private val SAND = Blocks.SAND.defaultState

        private val TALL_GRASS = Blocks.TALLGRASS.defaultState.withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS)
        private val DEAD_BUSH = Blocks.TALLGRASS.defaultState.withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.DEAD_BUSH)

        private val BUSH = Blocks.LEAVES.defaultState
    }

    lateinit var coverSelector: GenLayer
    lateinit var grassSelector: GenLayer

    override fun createLayers() {
        var layer: GenLayer = SelectionSeedLayer(2, 1)
        layer = GenLayerVoronoiZoom(1000, layer)
        layer = GenLayerFuzzyZoom(2000, layer)

        this.coverSelector = layer
        this.coverSelector.initWorldGenSeed(this.seed)

        var grass: GenLayer = SelectionSeedLayer(2, 3000)
        grass = GenLayerVoronoiZoom(1000, grass)
        grass = GenLayerFuzzyZoom(2000, grass)

        this.grassSelector = grass
        this.grassSelector.initWorldGenSeed(this.seed)
    }

    override fun coverDecorate(primer: ChunkPrimer, random: Random, x: Int, z: Int) {
        val grassLayer = this.sampleChunk(this.grassSelector, x, z)

        this.iterate { localX: Int, localZ: Int ->
            val bufferIndex = localX + localZ * 16
            val y = this.heightBuffer[bufferIndex]
            when (grassLayer[bufferIndex]) {
                0 -> {
                    if (random.nextInt(4) == 0) {
                        val state = if (random.nextInt(16) == 0) SparseVegetation.DEAD_BUSH else SparseVegetation.TALL_GRASS
                        primer.setBlockState(localX, y + 1, localZ, state)
                    }
                }
                1 -> {
                    if (random.nextInt(16) == 0) {
                        primer.setBlockState(localX, y + 1, localZ, SparseVegetation.BUSH)
                    }
                }
            }
        }
    }

    override fun getCover(x: Int, z: Int, random: Random) {
        this.coverLayer(this.coverBuffer, x, z, this.coverSelector) {
            when (it) {
                SparseVegetation.LAYER_DIRT -> SparseVegetation.DIRT
                SparseVegetation.LAYER_SAND -> SparseVegetation.SAND
                else -> SparseVegetation.DIRT
            }
        }
    }
}
