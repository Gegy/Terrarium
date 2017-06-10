package net.gegy1000.terrarium.server.map.glob.generator

import net.gegy1000.terrarium.server.map.glob.GlobType
import net.minecraft.world.gen.layer.GenLayer
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom

class VegetationWithCropland : MultiGlobGenerator(GlobType.VEGETATION_WITH_CROPLAND,
        Entry(GlobType.IRRIGATED_CROPS, 30),
        Entry(GlobType.GRASSLAND, 25),
        Entry(GlobType.SHRUBLAND, 25),
        Entry(GlobType.FOREST_SHRUBLAND_WITH_GRASS, 20)) {

    override fun zoom(layer: GenLayer): GenLayer {
        var zoom: GenLayer = GenLayerVoronoiZoom(1000, layer)
        zoom = GenLayerFuzzyZoom(2000, zoom)
        zoom = GenLayerVoronoiZoom(3000, zoom)
        return zoom
    }
}
