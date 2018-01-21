package net.gegy1000.terrarium.server.map.cover.generator;

import net.gegy1000.terrarium.server.map.LatitudinalZone;
import net.gegy1000.terrarium.server.map.cover.CoverGenerator;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.cover.generator.layer.ReplaceRandomLayer;
import net.gegy1000.terrarium.server.map.cover.generator.layer.SelectionSeedLayer;
import net.gegy1000.terrarium.server.map.cover.generator.primer.GlobPrimer;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.util.Random;

public class CroplandWithVegetationCover extends CoverGenerator {
    private static final int LAYER_GRASS = 0;
    private static final int LAYER_DIRT = 1;

    private GenLayer coverSelector;
    private GenLayer grassSelector;

    public CroplandWithVegetationCover() {
        super(CoverType.CROPLAND_WITH_VEGETATION);
    }

    @Override
    protected void createLayers(boolean debug) {
        GenLayer cover = new SelectionSeedLayer(2, 1);
        cover = new GenLayerVoronoiZoom(1000, cover);
        cover = new GenLayerFuzzyZoom(2000, cover);

        this.coverSelector = cover;
        this.coverSelector.initWorldGenSeed(this.seed);

        GenLayer grass = new SelectionSeedLayer(2, 3000);
        grass = new GenLayerFuzzyZoom(1000, grass);
        grass = new ReplaceRandomLayer(0, 1, 5, 2000, grass);
        grass = new ReplaceRandomLayer(1, 0, 5, 3000, grass);
        grass = new GenLayerFuzzyZoom(4000, grass);

        this.grassSelector = grass;
        this.grassSelector.initWorldGenSeed(this.seed);
    }

    @Override
    public void decorate(Random random, LatitudinalZone zone, int x, int z) {
        this.preventIntersection(3);

        this.decorateScatterSample(random, x, z, this.range(random, -1, 2), point -> {
            OAK_TALL_SHRUB.generate(this.world, random, point.pos);
        });

        this.decorateScatterSample(random, x, z, this.range(random, 5, 8), point -> {
            OAK_SMALL_SHRUB.generate(this.world, random, point.pos);
        });

        this.stopIntersectionPrevention();
    }

    @Override
    public void coverDecorate(GlobPrimer primer, Random random, int x, int z) {
        int[] grassLayer = this.sampleChunk(this.grassSelector, x, z);

        this.iterate(point -> {
            int index = point.index;
            int y = this.heightBuffer[index];
            if (grassLayer[index] == 1 && random.nextInt(3) != 0) {
                if (random.nextInt(8) == 0) {
                    primer.setBlockState(point.localX, y + 1, point.localZ, DOUBLE_TALL_GRASS);
                    primer.setBlockState(point.localX, y + 2, point.localZ, DOUBLE_TALL_GRASS.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER));
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
                case LAYER_GRASS:
                    return GRASS;
                case LAYER_DIRT:
                    return COARSE_DIRT;
                default:
                    return GRASS;
            }
        });
    }
}
