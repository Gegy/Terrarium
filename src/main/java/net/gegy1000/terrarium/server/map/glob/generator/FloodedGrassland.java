package net.gegy1000.terrarium.server.map.glob.generator;

import net.gegy1000.terrarium.server.map.glob.GlobGenerator;
import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.map.glob.generator.layer.ReplaceRandomLayer;
import net.gegy1000.terrarium.server.map.glob.generator.layer.SelectionSeedLayer;
import net.gegy1000.terrarium.server.map.glob.generator.primer.GlobPrimer;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.util.Random;

public class FloodedGrassland extends GlobGenerator {
    private static final int LAYER_WATER = 0;
    private static final int LAYER_PODZOL = 1;
    private static final int LAYER_DIRT = 2;

    private GenLayer coverSelector;
    private GenLayer grassSelector;

    public FloodedGrassland() {
        super(GlobType.FLOODED_GRASSLAND);
    }

    @Override
    protected void createLayers() {
        GenLayer cover = new SelectionSeedLayer(2, 1);
        cover = new GenLayerVoronoiZoom(1000, cover);
        cover = new ReplaceRandomLayer(LAYER_PODZOL, LAYER_DIRT, 4, 2000, cover);
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

        this.iterate(point -> {
            int index = point.index;
            if (grassLayer[index] == 1 && random.nextInt(6) != 0) {
                int y = this.heightBuffer[index];
                IBlockState state = primer.getBlockState(point.localX, y, point.localZ);
                if (state.getBlock() instanceof BlockLiquid) {
                    if (random.nextInt(10) == 0) {
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
                case LAYER_WATER:
                    if (random.nextInt(3) != 0) {
                        return WATER;
                    } else {
                        return PODZOL;
                    }
                case LAYER_PODZOL:
                    return PODZOL;
                default:
                    return COARSE_DIRT;
            }
        });
    }
}
