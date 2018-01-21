package net.gegy1000.terrarium.server.map.cover.generator;

import net.gegy1000.terrarium.server.map.cover.CoverGenerator;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.cover.generator.layer.SelectWeightedLayer;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.util.Random;

public class BareCover extends CoverGenerator {
    private static final int LAYER_DIRT = 0;
    private static final int LAYER_GRAVEL = 1;
    private static final int LAYER_SAND = 2;

    private GenLayer coverSelector;

    public BareCover(CoverType type) {
        super(type);
    }

    @Override
    protected void createLayers(boolean debug) {
        GenLayer layer = new SelectWeightedLayer(1,
                new SelectWeightedLayer.Entry(LAYER_GRAVEL, 2),
                new SelectWeightedLayer.Entry(LAYER_DIRT, 10),
                new SelectWeightedLayer.Entry(LAYER_SAND, 5));
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
