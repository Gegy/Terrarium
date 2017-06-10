package net.gegy1000.terrarium.server.map.glob.generator

import net.gegy1000.terrarium.server.map.glob.GlobGenerator
import net.gegy1000.terrarium.server.map.glob.GlobType
import net.gegy1000.terrarium.server.map.glob.generator.layer.SelectWeightedLayer
import net.minecraft.block.BlockDirt
import net.minecraft.init.Blocks
import net.minecraft.world.gen.layer.GenLayer
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom
import java.util.Random

class Bare : GlobGenerator(GlobType.BARE) {
    companion object {
        private const val LAYER_DIRT = 0
        private const val LAYER_GRAVEL = 1
        private const val LAYER_SAND = 2

        private val DIRT = Blocks.DIRT.defaultState.withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT)
        private val GRAVEL = Blocks.GRAVEL.defaultState
        private val SAND = Blocks.SAND.defaultState
    }

    lateinit var coverSelector: GenLayer

    override fun createLayers() {
        var layer: GenLayer = SelectWeightedLayer(1,
                SelectWeightedLayer.Entry(LAYER_GRAVEL, 10),
                SelectWeightedLayer.Entry(LAYER_DIRT, 5),
                SelectWeightedLayer.Entry(LAYER_SAND, 2))

        layer = GenLayerVoronoiZoom(1000, layer)
        layer = GenLayerFuzzyZoom(2000, layer)

        this.coverSelector = layer
        this.coverSelector.initWorldGenSeed(this.seed)
    }

    override fun getCover(x: Int, z: Int, random: Random) {
        this.coverLayer(this.coverBuffer, x, z, this.coverSelector) {
            when (it) {
                Bare.LAYER_DIRT -> Bare.DIRT
                Bare.LAYER_GRAVEL -> Bare.GRAVEL
                Bare.LAYER_SAND -> Bare.SAND
                else -> Bare.DIRT
            }
        }
    }
}
