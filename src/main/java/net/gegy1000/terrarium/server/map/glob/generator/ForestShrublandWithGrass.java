package net.gegy1000.terrarium.server.map.glob.generator;

import net.gegy1000.terrarium.server.map.glob.GlobGenerator;
import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.map.glob.generator.layer.ReplaceRandomLayer;
import net.gegy1000.terrarium.server.map.glob.generator.layer.SelectWeightedLayer;
import net.gegy1000.terrarium.server.map.glob.generator.layer.SelectionSeedLayer;
import net.gegy1000.terrarium.server.map.glob.generator.primer.GlobPrimer;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.util.Random;

public class ForestShrublandWithGrass extends GlobGenerator {
    private static final int LAYER_GRASS = 0;
    private static final int LAYER_DIRT = 1;
    private static final int LAYER_PODZOL = 2;

    private GenLayer coverSelector;
    private GenLayer grassSelector;

    public ForestShrublandWithGrass() {
        super(GlobType.FOREST_SHRUBLAND_WITH_GRASS);
    }

    @Override
    protected void createLayers() {
        GenLayer cover = new SelectWeightedLayer(1,
                new SelectWeightedLayer.Entry(LAYER_DIRT, 2),
                new SelectWeightedLayer.Entry(LAYER_GRASS, 8));
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
    public void coverDecorate(GlobPrimer primer, Random random, int x, int z) {
        int[] grassLayer = this.sampleChunk(this.grassSelector, x, z);
        this.iterate(pos -> {
            int y = this.heightBuffer[pos.index];
            if (grassLayer[pos.index] == 1 && random.nextInt(4) != 0) {
                primer.setBlockState(pos.localX, y + 1, pos.localZ, TALL_GRASS);
            } else if (random.nextInt(8) == 0) {
                primer.setBlockState(pos.localX, y + 1, pos.localZ, BUSH);
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
                    return PODZOL;
            }
        });
    }
}
