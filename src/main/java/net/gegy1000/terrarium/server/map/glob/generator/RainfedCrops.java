package net.gegy1000.terrarium.server.map.glob.generator;

import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.map.glob.generator.layer.SelectWeightedLayer;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;

import java.util.Random;

public class RainfedCrops extends Cropland {
    private static final int FARMLAND_LAYER = 0;
    private static final int WATER_LAYER = 1;
    private static final int DIRT_LAYER = 2;

    private GenLayer coverSelector;

    public RainfedCrops() {
        super(GlobType.RAINFED_CROPS);
    }

    @Override
    protected void createLayers() {
        super.createLayers();

        GenLayer cover = new SelectWeightedLayer(50,
                new SelectWeightedLayer.Entry(FARMLAND_LAYER, 10),
                new SelectWeightedLayer.Entry(WATER_LAYER, 3),
                new SelectWeightedLayer.Entry(DIRT_LAYER, 5));
        cover = new GenLayerFuzzyZoom(5, cover);
        cover = new GenLayerFuzzyZoom(2000, cover);

        this.coverSelector = cover;
        this.coverSelector.initWorldGenSeed(this.seed);
    }

    @Override
    public void getCover(Random random, int x, int z) {
        this.coverLayer(this.coverBuffer, x, z, this.coverSelector, type -> {
            switch (type) {
                case 0:
                    return FARMLAND;
                case 1:
                    return WATER;
                default:
                    return COARSE_DIRT;
            }
        });
    }
}
