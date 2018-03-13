package net.gegy1000.terrarium.server.world.cover.generator;

import net.gegy1000.earth.server.world.cover.LatitudinalZone;
import net.gegy1000.terrarium.server.world.cover.CoverGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectWeightedLayer;
import net.gegy1000.terrarium.server.world.cover.generator.primer.GlobPrimer;
import net.minecraft.block.state.IBlockState;
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
    public void decorate(Random random, LatitudinalZone zone, int x, int z) {
        this.preventIntersection(5);

        this.decorateScatterSample(random, x, z, this.range(random, -16, 1), point -> {
            if (this.slopeBuffer[point.chunk.index] < MOUNTAINOUS_SLOPE) {
                OAK_TALL_SHRUB.generate(this.world, random, point.pos);
            }
        });

        this.stopIntersectionPrevention();
    }

    @Override
    public void coverDecorate(GlobPrimer primer, Random random, int x, int z) {
        this.iterate(point -> {
            byte slope = this.slopeBuffer[point.index];
            if (slope < MOUNTAINOUS_SLOPE && random.nextInt(250) == 0) {
                int y = this.heightBuffer[point.index];
                primer.setBlockState(point.localX, y + 1, point.localZ, DEAD_BUSH);
            }
        });
    }

    @Override
    public void getCover(Random random, int x, int z) {
        this.getCover(this.coverBlockBuffer, x, z);
    }

    @Override
    public void getFiller(Random random, int x, int z) {
        this.getCover(this.fillerBlockBuffer, x, z);
    }

    private void getCover(IBlockState[] buffer, int x, int z) {
        this.coverLayer(buffer, x, z, this.coverSelector, type -> {
            int slope = type.getSlope();
            switch (type.getCoverType()) {
                case LAYER_GRAVEL:
                    return slope >= MOUNTAINOUS_SLOPE ? COBBLESTONE : GRAVEL;
                case LAYER_SAND:
                    return slope >= MOUNTAINOUS_SLOPE ? SANDSTONE : SAND;
                default:
                    return slope >= MOUNTAINOUS_SLOPE ? HARDENED_CLAY : SAND;
            }
        });
    }
}
