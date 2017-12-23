package net.gegy1000.terrarium.server.map.glob.generator;

import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

public class VegetationWithCropland extends MultiGlobGenerator {
    public VegetationWithCropland() {
        super(GlobType.VEGETATION_WITH_CROPLAND, new Entry(GlobType.IRRIGATED_CROPS, 30),
                new Entry(GlobType.GRASSLAND, 25),
                new Entry(GlobType.SHRUBLAND, 25),
                new Entry(GlobType.FOREST_SHRUBLAND_WITH_GRASS, 20));
    }

    @Override
    protected GenLayer zoom(GenLayer layer) {
        GenLayer zoom = new GenLayerVoronoiZoom(1000, layer);
        zoom = new GenLayerFuzzyZoom(2000, zoom);
        zoom = new GenLayerVoronoiZoom(3000, zoom);
        return zoom;
    }
}
