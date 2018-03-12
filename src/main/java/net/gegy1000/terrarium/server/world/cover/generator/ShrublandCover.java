package net.gegy1000.terrarium.server.world.cover.generator;

import net.gegy1000.terrarium.server.world.LatitudinalZone;
import net.gegy1000.terrarium.server.world.cover.CoverGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectionSeedLayer;
import net.gegy1000.terrarium.server.world.cover.generator.primer.GlobPrimer;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.util.Random;

public class ShrublandCover extends CoverGenerator {
    private static final int LAYER_GRASS = 0;
    private static final int LAYER_DIRT = 1;

    private GenLayer coverSelector;
    private GenLayer grassSelector;

    public ShrublandCover() {
        super(CoverType.SHRUBLAND);
    }

    @Override
    protected void createLayers(boolean debug) {
        GenLayer cover = new SelectionSeedLayer(2, 1);
        cover = new GenLayerVoronoiZoom(1000, cover);
        cover = new GenLayerFuzzyZoom(3000, cover);

        this.coverSelector = cover;
        this.coverSelector.initWorldGenSeed(this.seed);

        GenLayer grass = new SelectionSeedLayer(3, 3000);
        grass = new GenLayerVoronoiZoom(1000, grass);
        grass = new GenLayerFuzzyZoom(2000, grass);

        this.grassSelector = grass;
        this.grassSelector.initWorldGenSeed(this.seed);
    }

    @Override
    public void decorate(Random random, LatitudinalZone zone, int x, int z) {
        this.preventIntersection(2);

        int oakShrubCount = this.getOakShrubCount(random, zone);
        this.decorateScatterSample(random, x, z, oakShrubCount, point -> {
            OAK_TALL_SHRUB.generate(this.world, random, point.pos);
        });
        this.decorateScatterSample(random, x, z, oakShrubCount, point -> {
            OAK_SMALL_SHRUB.generate(this.world, random, point.pos);
        });

        int acaciaShrubCount = this.getAcaciaShrubCount(random, zone);
        this.decorateScatterSample(random, x, z, acaciaShrubCount, point -> {
            ACACIA_TALL_SHRUB.generate(this.world, random, point.pos);
        });
        this.decorateScatterSample(random, x, z, acaciaShrubCount, point -> {
            ACACIA_SMALL_SHRUB.generate(this.world, random, point.pos);
        });

        this.stopIntersectionPrevention();
    }

    @Override
    public void coverDecorate(GlobPrimer primer, Random random, int x, int z) {
        int[] grassLayer = this.sampleChunk(this.grassSelector, x, z);
        this.iterate(point -> {
            int grassType = grassLayer[point.index];
            if (grassType != 0 && random.nextInt(4) == 0) {
                int y = this.heightBuffer[point.index];
                if (grassType == 1) {
                    primer.setBlockState(point.localX, y + 1, point.localZ, TALL_GRASS);
                }
            }
        });
    }

    @Override
    public void getCover(Random random, int x, int z) {
        this.coverLayer(this.coverBlockBuffer, x, z, this.coverSelector, type -> {
            switch (type.getCoverType()) {
                case LAYER_GRASS:
                    return GRASS;
                case LAYER_DIRT:
                    return COARSE_DIRT;
                default:
                    return GRASS;
            }
        });
    }

    private int getOakShrubCount(Random random, LatitudinalZone zone) {
        switch (zone) {
            case TROPICS:
            case SUBTROPICS:
                return this.range(random, 2, 5);
            default:
                return this.range(random, 1, 3);
        }
    }

    private int getAcaciaShrubCount(Random random, LatitudinalZone zone) {
        switch (zone) {
            case TROPICS:
            case SUBTROPICS:
                return this.range(random, 1, 3);
            default:
                return this.range(random, 2, 5);
        }
    }
}
