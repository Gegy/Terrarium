package net.gegy1000.terrarium.server.map.glob.generator;

import net.gegy1000.terrarium.server.map.glob.GlobGenerator;
import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.map.glob.generator.layer.SelectWeightedLayer;
import net.gegy1000.terrarium.server.map.glob.generator.primer.GlobPrimer;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.util.Random;

public class RainfedCrops extends GlobGenerator {
    protected static final int LAYER_GRASS = 0;
    protected static final int LAYER_PODZOL = 1;

    private static final int LAYER_SHORT_GRASS = 0;
    private static final int LAYER_TALL_GRASS = 1;

    private GenLayer coverSelector;
    private GenLayer grassSelector;

    public RainfedCrops() {
        super(GlobType.RAINFED_CROPS);
    }

    @Override
    protected void createLayers() {
        GenLayer cover = new SelectWeightedLayer(10,
                new SelectWeightedLayer.Entry(LAYER_GRASS, 10),
                new SelectWeightedLayer.Entry(LAYER_PODZOL, 4));
        cover = new GenLayerVoronoiZoom(2000, cover);
        cover = new GenLayerFuzzyZoom(3000, cover);

        this.coverSelector = cover;
        this.coverSelector.initWorldGenSeed(this.seed);

        GenLayer grass = new SelectWeightedLayer(50,
                new SelectWeightedLayer.Entry(LAYER_SHORT_GRASS, 10),
                new SelectWeightedLayer.Entry(LAYER_TALL_GRASS, 5));
        grass = new GenLayerFuzzyZoom(4000, grass);
        grass = new GenLayerFuzzyZoom(5000, grass);

        this.grassSelector = grass;
        this.grassSelector.initWorldGenSeed(this.seed);
    }

    @Override
    public void getCover(Random random, int x, int z) {
        this.coverLayer(this.coverBuffer, x, z, this.coverSelector, type -> {
            switch (type) {
                case LAYER_GRASS:
                    return GRASS;
                default:
                    return PODZOL;
            }
        });
    }

    @Override
    public void coverDecorate(GlobPrimer primer, Random random, int x, int z) {
        int[] grassLayer = this.sampleChunk(this.grassSelector, x, z);
        this.iterate(point -> {
            int y = this.heightBuffer[point.index];
            switch (grassLayer[point.index]) {
                case LAYER_SHORT_GRASS:
                    if (random.nextInt(3) == 0) {
                        primer.setBlockState(point.localX, y + 1, point.localZ, TALL_GRASS);
                    }
                    break;
                case LAYER_TALL_GRASS:
                    if (random.nextInt(2) == 0) {
                        primer.setBlockState(point.localX, y + 1, point.localZ, DOUBLE_TALL_GRASS);
                        primer.setBlockState(point.localX, y + 2, point.localZ, DOUBLE_TALL_GRASS.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER));
                    }
                    break;
            }
        });
    }

    @Override
    protected IBlockState getCoverAt(Random random, int x, int z) {
        return GRASS;
    }
}
