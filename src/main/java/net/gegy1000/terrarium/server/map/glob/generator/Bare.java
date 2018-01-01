package net.gegy1000.terrarium.server.map.glob.generator;

import net.gegy1000.terrarium.server.map.glob.GlobGenerator;
import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.map.glob.generator.layer.SelectWeightedLayer;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.util.Random;

public class Bare extends GlobGenerator {
    private static final int LAYER_DIRT = 0;
    private static final int LAYER_GRAVEL = 1;
    private static final int LAYER_SAND = 2;

    private GenLayer coverSelector;

    public Bare(GlobType type) {
        super(type);
    }

    @Override
    protected void createLayers() {
        GenLayer layer = new SelectWeightedLayer(1,
                new SelectWeightedLayer.Entry(LAYER_GRAVEL, 10),
                new SelectWeightedLayer.Entry(LAYER_DIRT, 5),
                new SelectWeightedLayer.Entry(LAYER_SAND, 2));
        layer = new GenLayerVoronoiZoom(1000, layer);
        layer = new GenLayerFuzzyZoom(2000, layer);

        this.coverSelector = layer;
        this.coverSelector.initWorldGenSeed(this.seed);
    }

    @Override
    public void getCover(Random random, int x, int z) {
        this.coverLayer(this.coverBuffer, x, z, this.coverSelector, type -> {
            switch (type) {
                case LAYER_DIRT:
                    return COARSE_DIRT;
                case LAYER_GRAVEL:
                    return GRAVEL;
                case LAYER_SAND:
                    return SAND;
                default:
                    return COARSE_DIRT;
            }
        });
    }
}
