package net.gegy1000.terrarium.server.map.glob.generator;

import net.gegy1000.terrarium.server.map.glob.GlobGenerator;
import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.map.glob.generator.layer.SelectionSeedLayer;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.IntCache;

import java.util.Random;

public class Water extends GlobGenerator {
    private static final IBlockState WATER = Blocks.WATER.getDefaultState();
    private static final IBlockState SAND = Blocks.SAND.getDefaultState();
    private static final IBlockState DIRT = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
    private static final IBlockState CLAY = Blocks.CLAY.getDefaultState();
    private static final IBlockState GRAVEL = Blocks.GRAVEL.getDefaultState();

    private GenLayer coverSelector;

    public Water() {
        super(GlobType.WATER);
    }

    @Override
    protected void createLayers() {
        GenLayer layer = new SelectionSeedLayer(2, 1);
        layer = new GenLayerFuzzyZoom(1000, layer);
        layer = new CoverLayer(2000, layer);
        layer = new GenLayerVoronoiZoom(3000, layer);
        layer = new GenLayerFuzzyZoom(4000, layer);

        this.coverSelector = layer;
        this.coverSelector.initWorldGenSeed(this.seed);
    }

    @Override
    protected IBlockState getCoverAt(Random random, int x, int z) {
        return WATER;
    }

    @Override
    public void getFiller(Random random, int x, int z) {
        this.coverLayer(this.fillerBuffer, x, z, this.coverSelector, type -> {
            switch (type) {
                case 0:
                    return SAND;
                case 1:
                    return GRAVEL;
                case 2:
                    return DIRT;
                case 3:
                    return CLAY;
                default:
                    return DIRT;
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
