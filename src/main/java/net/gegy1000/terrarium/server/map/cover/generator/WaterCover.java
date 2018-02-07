package net.gegy1000.terrarium.server.map.cover.generator;

import net.gegy1000.terrarium.server.map.cover.CoverGenerator;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.cover.generator.layer.SelectionSeedLayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.IntCache;

import java.util.Random;

public class WaterCover extends CoverGenerator {
    private GenLayer coverSelector;

    public WaterCover() {
        super(CoverType.WATER);
    }

    @Override
    protected void createLayers(boolean debug) {
        GenLayer layer = new SelectionSeedLayer(2, 1);
        layer = new GenLayerFuzzyZoom(1000, layer);
        layer = new CoverLayer(2000, layer);
        layer = new GenLayerVoronoiZoom(3000, layer);
        layer = new GenLayerFuzzyZoom(4000, layer);

        this.coverSelector = layer;
        this.coverSelector.initWorldGenSeed(this.seed);
    }

    @Override
    protected IBlockState getCoverAt(Random random, int x, int z, int slope) {
        return WATER;
    }

    @Override
    public void getFiller(Random random, int x, int z) {
        this.coverLayer(this.fillerBuffer, x, z, this.coverSelector, type -> {
            switch (type.getCoverType()) {
                case 0:
                    return SAND;
                case 1:
                    return GRAVEL;
                case 2:
                    return COARSE_DIRT;
                case 3:
                    return CLAY;
                default:
                    return COARSE_DIRT;
            }
        });
    }

    private class CoverLayer extends GenLayer {
        public CoverLayer(long seed, GenLayer parent) {
            super(seed);
            this.parent = parent;
        }

        @Override
        public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
            int[] parent = this.parent.getInts(areaX, areaY, areaWidth, areaHeight);
            int[] result = IntCache.getIntCache(areaWidth * areaHeight);
            for (int z = 0; z < areaHeight; z++) {
                for (int x = 0; x < areaWidth; x++) {
                    this.initChunkSeed(areaX + x, areaY + z);
                    int index = x + z * areaWidth;
                    int sample = parent[index];
                    if (sample == 0) {
                        result[index] = this.nextInt(2);
                    } else {
                        result[index] = this.nextInt(20) == 0 ? 3 : 2;
                    }
                }
            }
            return result;
        }
    }
}
