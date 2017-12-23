package net.gegy1000.terrarium.server.map.glob.generator;

import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

public class CroplandWithVegetation extends MultiGlobGenerator {
    public CroplandWithVegetation() {
        super(GlobType.CROPLAND_WITH_VEGETATION, new Entry(GlobType.IRRIGATED_CROPS, 60),
                new Entry(GlobType.GRASSLAND, 15),
                new Entry(GlobType.SHRUBLAND, 15),
                new Entry(GlobType.FOREST_SHRUBLAND_WITH_GRASS, 10));
    }

    @Override
    protected GenLayer zoom(GenLayer layer) {
        GenLayer zoom = new GenLayerVoronoiZoom(1000, layer);
        zoom = new GenLayerFuzzyZoom(2000, zoom);
        zoom = new GenLayerVoronoiZoom(3000, zoom);
        return zoom;
    }
}
