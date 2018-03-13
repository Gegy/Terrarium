package net.gegy1000.terrarium.server.world.cover.generator;

import net.gegy1000.earth.server.world.cover.LatitudinalZone;
import net.gegy1000.terrarium.server.world.cover.CoverGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.ReplaceRandomLayer;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectionSeedLayer;
import net.gegy1000.terrarium.server.world.cover.generator.primer.GlobPrimer;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.util.Random;

public class GrasslandCover extends CoverGenerator {
    private static final int LAYER_GRASS = 0;
    private static final int LAYER_DIRT = 1;
    private static final int LAYER_PODZOL = 2;

    private GenLayer coverSelector;
    private GenLayer grassSelector;

    public GrasslandCover() {
        super(CoverType.GRASSLAND);
    }

    @Override
    protected void createLayers(boolean debug) {
        GenLayer cover = new SelectionSeedLayer(2, 1);
        cover = new GenLayerVoronoiZoom(1000, cover);
        cover = new ReplaceRandomLayer(LAYER_DIRT, LAYER_PODZOL, 6, 2000, cover);
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
        this.decorateScatterSample(random, x, z, this.range(random, -1, 2), point -> {
            ACACIA_SMALL_SHRUB.generate(this.world, random, point.pos);
        });
    }

    @Override
    public void coverDecorate(GlobPrimer primer, Random random, int x, int z) {
        int[] grassLayer = this.sampleChunk(this.grassSelector, x, z);

        this.iterate(point -> {
            int index = point.index;
            if (grassLayer[index] == 1 && random.nextInt(4) != 0) {
                int y = this.heightBuffer[index];
                if (random.nextInt(4) == 0) {
                    primer.setBlockState(point.localX, y + 1, point.localZ, DOUBLE_TALL_GRASS);
                    primer.setBlockState(point.localX, y + 2, point.localZ, DOUBLE_TALL_GRASS.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER));
                } else if (random.nextInt(16) == 0) {
                    primer.setBlockState(point.localX, y + 1, point.localZ, GrasslandCover.DEAD_BUSH);
                } else {
                    primer.setBlockState(point.localX, y + 1, point.localZ, GrasslandCover.TALL_GRASS);
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
                    return PODZOL;
            }
        });
    }
}
