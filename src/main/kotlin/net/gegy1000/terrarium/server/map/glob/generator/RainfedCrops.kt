package net.gegy1000.terrarium.server.map.glob.generator

import net.gegy1000.terrarium.server.map.glob.GlobType
import net.gegy1000.terrarium.server.map.glob.generator.layer.SelectWeightedLayer
import net.minecraft.world.gen.layer.GenLayer
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom
import java.util.Random

class RainfedCrops : Cropland(GlobType.RAINFED_CROPS) {
    companion object {
        private const val FARMLAND_LAYER = 0
        private const val WATER_LAYER = 1
        private const val DIRT_LAYER = 2
    }

    lateinit var coverSelector: GenLayer

    override fun createLayers() {
        super.createLayers()

        var layer: GenLayer = SelectWeightedLayer(50,
                SelectWeightedLayer.Entry(FARMLAND_LAYER, 10),
                SelectWeightedLayer.Entry(WATER_LAYER, 3),
                SelectWeightedLayer.Entry(DIRT_LAYER, 5))

        layer = GenLayerFuzzyZoom(5, layer)
        layer = GenLayerFuzzyZoom(2000, layer)

        this.coverSelector = layer
        this.coverSelector.initWorldGenSeed(this.seed)
    }

    override fun getCover(x: Int, z: Int, random: Random) {
        this.coverLayer(this.coverBuffer, x, z, this.coverSelector) {
            when (it) {
                0 -> FARMLAND
                1 -> WATER
                else -> COARSE_DIRT
            }
        }
    }
}
