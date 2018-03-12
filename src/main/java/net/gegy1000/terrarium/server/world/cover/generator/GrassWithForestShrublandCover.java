package net.gegy1000.terrarium.server.world.cover.generator;

import net.gegy1000.terrarium.server.world.LatitudinalZone;
import net.gegy1000.terrarium.server.world.cover.CoverGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.ReplaceRandomLayer;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectWeightedLayer;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectionSeedLayer;
import net.gegy1000.terrarium.server.world.cover.generator.primer.GlobPrimer;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.util.Random;

public class GrassWithForestShrublandCover extends CoverGenerator {
    private static final int LAYER_GRASS = 0;
    private static final int LAYER_DIRT = 1;
    private static final int LAYER_PODZOL = 2;

    private GenLayer coverSelector;
    private GenLayer grassSelector;

    public GrassWithForestShrublandCover() {
        super(CoverType.GRASS_WITH_FOREST_SHRUBLAND);
    }

    @Override
    protected void createLayers(boolean debug) {
        GenLayer cover = new SelectWeightedLayer(1,
                new SelectWeightedLayer.Entry(LAYER_GRASS, 2),
                new SelectWeightedLayer.Entry(LAYER_DIRT, 8));
        cover = new GenLayerVoronoiZoom(1000, cover);
        cover = new ReplaceRandomLayer(LAYER_DIRT, LAYER_PODZOL, 4, 2000, cover);
        cover = new GenLayerFuzzyZoom(3000, cover);

        this.coverSelector = cover;
        this.coverSelector.initWorldGenSeed(this.seed);

        GenLayer grass = new SelectionSeedLayer(2, 3000);
        grass = new GenLayerVoronoiZoom(1000, grass);
        grass = new GenLayerFuzzyZoom(2000, grass);

        this.grassSelector = grass;
        this.grassSelector.initWorldGenSeed(this.seed);
    }

    @Override
    public void decorate(Random random, LatitudinalZone zone, int x, int z) {
        this.preventIntersection(2);

        this.decorateScatterSample(random, x, z, this.getOakShrubCount(random, zone), point -> {
            OAK_TALL_SHRUB.generate(this.world, random, point.pos);
        });

        this.decorateScatterSample(random, x, z, this.getJungleShrubCount(random, zone), point -> {
            JUNGLE_TALL_SHRUB.generate(this.world, random, point.pos);
        });

        this.stopIntersectionPrevention();
    }

    @Override
    public void coverDecorate(GlobPrimer primer, Random random, int x, int z) {
        int[] grassLayer = this.sampleChunk(this.grassSelector, x, z);
        this.iterate(point -> {
            int y = this.heightBuffer[point.index];
            if (grassLayer[point.index] == 1 && random.nextInt(4) != 0) {
                primer.setBlockState(point.localX, y + 1, point.localZ, TALL_GRASS);
            } else if (random.nextInt(10) == 0) {
                primer.setBlockState(point.localX, y + 1, point.localZ, BUSH);
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
                    return PODZOL;
            }
        });
    }

    private int getOakShrubCount(Random random, LatitudinalZone zone) {
        switch (zone) {
            case TROPICS:
            case SUBTROPICS:
                return this.range(random, -1, 2);
            default:
                return this.range(random, 1, 4);
        }
    }

    private int getJungleShrubCount(Random random, LatitudinalZone zone) {
        switch (zone) {
            case TROPICS:
            case SUBTROPICS:
                return this.range(random, 1, 4);
            default:
                return this.range(random, -1, 2);
        }
    }
}
