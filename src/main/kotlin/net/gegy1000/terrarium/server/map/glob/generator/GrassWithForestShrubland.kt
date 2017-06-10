package net.gegy1000.terrarium.server.map.glob.generator

import net.gegy1000.terrarium.server.map.glob.GlobGenerator
import net.gegy1000.terrarium.server.map.glob.GlobType
import net.gegy1000.terrarium.server.map.glob.generator.layer.ReplaceRandomLayer
import net.gegy1000.terrarium.server.map.glob.generator.layer.SelectWeightedLayer
import net.gegy1000.terrarium.server.map.glob.generator.layer.SelectionSeedLayer
import net.minecraft.block.BlockDirt
import net.minecraft.block.BlockTallGrass
import net.minecraft.init.Blocks
import net.minecraft.world.chunk.ChunkPrimer
import net.minecraft.world.gen.layer.GenLayer
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom
import java.util.Random

class GrassWithForestShrubland : GlobGenerator(GlobType.GRASS_WITH_FOREST_SHRUBLAND) {
    companion object {
        private const val LAYER_GRASS = 0
        private const val LAYER_DIRT = 1
        private const val LAYER_PODZOL = 2

        private val GRASS = Blocks.GRASS.defaultState
        private val DIRT = Blocks.DIRT.defaultState.withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT)
        private val PODZOL = Blocks.DIRT.defaultState.withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL)

        private val TALL_GRASS = Blocks.TALLGRASS.defaultState.withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS)
        private val BUSH = Blocks.LEAVES.defaultState
    }

    lateinit var coverSelector: GenLayer
    lateinit var grassSelector: GenLayer

    override fun createLayers() {
        var cover: GenLayer = SelectWeightedLayer(1,
                SelectWeightedLayer.Entry(GrassWithForestShrubland.LAYER_GRASS, 2),
                SelectWeightedLayer.Entry(GrassWithForestShrubland.LAYER_DIRT, 8))

        cover = GenLayerVoronoiZoom(1000, cover)
        cover = ReplaceRandomLayer(replace = LAYER_DIRT, replacement = LAYER_PODZOL, chance = 4, seed = 2000, parent = cover)
        cover = GenLayerFuzzyZoom(3000, cover)

        this.coverSelector = cover
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

            if (grassLayer[bufferIndex] == 1 && random.nextInt(4) != 0) {
                primer.setBlockState(localX, y + 1, localZ, GrassWithForestShrubland.TALL_GRASS)
            } else if (random.nextInt(6) == 0) {
                primer.setBlockState(localX, y + 1, localZ, GrassWithForestShrubland.BUSH)
            }
        }
    }

    override fun getCover(x: Int, z: Int, random: Random) {
        this.coverLayer(this.coverBuffer, x, z, this.coverSelector) {
            when (it) {
                GrassWithForestShrubland.LAYER_GRASS -> GrassWithForestShrubland.GRASS
                GrassWithForestShrubland.LAYER_DIRT -> GrassWithForestShrubland.DIRT
                else -> GrassWithForestShrubland.PODZOL
            }
        }
    }
}
