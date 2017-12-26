package net.gegy1000.terrarium.server.map.glob.generator;

import net.gegy1000.terrarium.server.map.glob.GlobGenerator;
import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.map.glob.generator.layer.SelectionSeedLayer;
import net.gegy1000.terrarium.server.map.glob.generator.primer.GlobPrimer;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.util.Random;

public class SparseVegetation extends GlobGenerator {
    private static final int LAYER_DIRT = 0;
    private static final int LAYER_SAND = 1;

    private static final IBlockState SAND = Blocks.SAND.getDefaultState();
    private static final IBlockState DIRT = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);

    private static final IBlockState TALL_GRASS = Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS);
    private static final IBlockState DEAD_BUSH = Blocks.DEADBUSH.getDefaultState();

    private static final IBlockState BUSH = Blocks.LEAVES.getDefaultState().withProperty(BlockLeaves.CHECK_DECAY, false).withProperty(BlockLeaves.DECAYABLE, false);

    private GenLayer coverSelector;
    private GenLayer grassSelector;

    public SparseVegetation() {
        super(GlobType.SPARSE_VEGETATION);
    }

    @Override
    protected void createLayers() {
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
    public void coverDecorate(GlobPrimer primer, Random random, int x, int z) {
        int[] grassLayer = this.sampleChunk(this.grassSelector, x, z);
        this.iterate(point -> {
            int y = this.heightBuffer[point.index];
            switch (grassLayer[point.index]) {
                case 0:
                    if (random.nextInt(4) == 0) {
                        IBlockState state = random.nextInt(16) == 0 ? SparseVegetation.DEAD_BUSH : SparseVegetation.TALL_GRASS;
                        primer.setBlockState(point.localX, y + 1, point.localZ, state);
                    }
                    break;
                case 1:
                    if (random.nextInt(16) == 0) {
                        primer.setBlockState(point.localX, y + 1, point.localZ, SparseVegetation.BUSH);
                    }
                    break;
            }
        });
    }

    @Override
    public void getCover(Random random, int x, int z) {
        this.coverLayer(this.coverBuffer, x, z, this.coverSelector, type -> {
            switch (type) {
                case LAYER_SAND:
                    return SAND;
                case LAYER_DIRT:
                    return DIRT;
                default:
                    return SAND;
            }
        });
    }
}
