package net.gegy1000.terrarium.server.map.cover.generator;

import net.gegy1000.terrarium.server.map.cover.CoverGenerator;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.cover.generator.layer.ReplaceRandomLayer;
import net.gegy1000.terrarium.server.map.cover.generator.layer.SelectWeightedLayer;
import net.gegy1000.terrarium.server.map.cover.generator.layer.SelectionSeedLayer;
import net.gegy1000.terrarium.server.map.cover.generator.primer.GlobPrimer;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.util.Random;

public abstract class ForestCover extends CoverGenerator {
    private static final int HEIGHT_STEP = 2;

    protected static final int LAYER_PRIMARY = 0;
    protected static final int LAYER_DIRT = 1;
    protected static final int LAYER_PODZOL = 2;

    protected GenLayer coverSelector;
    protected GenLayer clearingSelector;
    protected GenLayer heightOffsetSelector;

    public ForestCover(CoverType type) {
        super(type);
    }

    @Override
    protected void createLayers(boolean debug) {
        this.coverSelector = this.createCoverSelector();
        this.coverSelector.initWorldGenSeed(this.seed);

        if (!debug) {
            GenLayer clearing = new SelectWeightedLayer(2,
                    new SelectWeightedLayer.Entry(0, 6),
                    new SelectWeightedLayer.Entry(1, 4));
            clearing = new GenLayerVoronoiZoom(4000, clearing);
            clearing = new ReplaceRandomLayer(0, 2, 16, 6000, clearing);
            clearing = new ReplaceRandomLayer(1, 0, 10, 7000, clearing);
            clearing = new GenLayerFuzzyZoom(5000, clearing);
            clearing = new GenLayerVoronoiZoom(8000, clearing);
            clearing = new GenLayerFuzzyZoom(9000, clearing);

            this.clearingSelector = clearing;
        } else {
            this.clearingSelector = new SelectionSeedLayer(1, 0);
        }

        this.clearingSelector.initWorldGenSeed(this.seed);

        GenLayer heightOffset = new SelectionSeedLayer((this.getMaxHeightOffset() / HEIGHT_STEP) + 1, 3);
        heightOffset = new GenLayerVoronoiZoom(10000, heightOffset);
        heightOffset = new GenLayerFuzzyZoom(11000, heightOffset);
        heightOffset = new GenLayerVoronoiZoom(12000, heightOffset);
        heightOffset = new GenLayerFuzzyZoom(13000, heightOffset);
        heightOffset = new GenLayerVoronoiZoom(14000, heightOffset);
        heightOffset = new GenLayerFuzzyZoom(15000, heightOffset);

        this.heightOffsetSelector = heightOffset;
        this.heightOffsetSelector.initWorldGenSeed(this.seed);
    }

    @Override
    public void coverDecorate(GlobPrimer primer, Random random, int x, int z) {
        this.iterate(point -> {
            if (random.nextInt(4) == 0) {
                int y = this.heightBuffer[point.index];
                IBlockState state = primer.getBlockState(point.localX, y, point.localZ);
                if (state.getBlock() instanceof BlockLiquid) {
                    if (random.nextInt(16) == 0) {
                        primer.setBlockState(point.localX, y + 1, point.localZ, LILYPAD);
                    }
                } else {
                    primer.setBlockState(point.localX, y + 1, point.localZ, TALL_GRASS);
                }
            }
        });
    }

    @Override
    public void getCover(Random random, int x, int z) {
        this.coverLayer(this.coverBuffer, x, z, this.coverSelector, type -> {
            switch (type) {
                case LAYER_PRIMARY:
                    return GRASS;
                case LAYER_DIRT:
                    return COARSE_DIRT;
                default:
                    return PODZOL;
            }
        });
    }

    protected int sampleHeightOffset(int[] heightOffsetLayer, ChunkPoint point) {
        return heightOffsetLayer[point.index] * HEIGHT_STEP;
    }

    public GenLayer createCoverSelector() {
        GenLayer cover = new SelectionSeedLayer(2, 1);
        cover = new GenLayerVoronoiZoom(1000, cover);
        cover = new ReplaceRandomLayer(ForestCover.LAYER_DIRT, ForestCover.LAYER_PODZOL, 4, 2000, cover);
        cover = new GenLayerFuzzyZoom(3000, cover);
        return cover;
    }

    public int getMaxHeightOffset() {
        return 0;
    }
}
