package net.gegy1000.terrarium.server.map.cover.generator;

import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.cover.generator.layer.OutlineEdgeLayer;
import net.gegy1000.terrarium.server.map.cover.generator.layer.ReplaceRandomLayer;
import net.gegy1000.terrarium.server.map.cover.generator.layer.SelectionSeedLayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;

import java.util.Random;

public abstract class FloodedForestCover extends ForestCover {
    protected GenLayer waterSelector;

    public FloodedForestCover(CoverType type) {
        super(type);
    }

    @Override
    protected void createLayers(boolean debug) {
        super.createLayers(debug);

        GenLayer water = new SelectionSeedLayer(2, 2);
        water = new GenLayerFuzzyZoom(11000, water);
        water = new GenLayerVoronoiZoom(12000, water);
        water = new OutlineEdgeLayer(3, 13000, water);
        water = new GenLayerZoom(14000, water);

        this.waterSelector = water;
        this.waterSelector.initWorldGenSeed(this.seed);
    }

    @Override
    public GenLayer createCoverSelector() {
        GenLayer cover = new SelectionSeedLayer(2, 1);
        if (this.hasPodzol()) {
            cover = new ReplaceRandomLayer(LAYER_PRIMARY, LAYER_PODZOL, 2, 6000, cover);
        }
        cover = new GenLayerVoronoiZoom(7000, cover);
        if (this.hasPodzol()) {
            cover = new ReplaceRandomLayer(LAYER_PODZOL, LAYER_DIRT, 3, 8000, cover);
        }
        cover = new GenLayerFuzzyZoom(9000, cover);
        return cover;
    }

    @Override
    public void getCover(Random random, int x, int z) {
        int[] cover = this.sampleChunk(this.coverSelector, x, z);
        int[] water = this.sampleChunk(this.waterSelector, x, z);
        this.iterate(point -> {
            int index = point.index;
            if (water[index] == 3) {
                this.coverBuffer[index] = WATER;
            } else {
                switch (cover[index]) {
                    case LAYER_PRIMARY:
                        this.coverBuffer[index] = this.getPrimaryCover();
                        break;
                    case LAYER_PODZOL:
                        this.coverBuffer[index] = PODZOL;
                        break;
                    default:
                        this.coverBuffer[index] = COARSE_DIRT;
                        break;
                }
            }
        });
    }

    @Override
    public int getMaxHeightOffset() {
        return 5;
    }

    public abstract IBlockState getPrimaryCover();

    public abstract boolean hasPodzol();

    @Override
    protected IBlockState getFillerAt(Random random, int x, int z) {
        return COARSE_DIRT;
    }
}
