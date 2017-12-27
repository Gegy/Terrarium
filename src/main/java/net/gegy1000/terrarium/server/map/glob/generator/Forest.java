package net.gegy1000.terrarium.server.map.glob.generator;

import net.gegy1000.terrarium.server.map.glob.GlobGenerator;
import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.map.glob.generator.layer.ReplaceRandomLayer;
import net.gegy1000.terrarium.server.map.glob.generator.layer.SelectWeightedLayer;
import net.gegy1000.terrarium.server.map.glob.generator.layer.SelectionSeedLayer;
import net.gegy1000.terrarium.server.map.glob.generator.primer.GlobPrimer;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.feature.WorldGenTaiga1;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.util.Random;

public class Forest extends GlobGenerator {
    protected static final IBlockState OAK_LOG = Blocks.LOG.getDefaultState();
    protected static final IBlockState OAK_LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockLeaves.CHECK_DECAY, false);

    protected static final IBlockState JUNGLE_LOG = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
    protected static final IBlockState JUNGLE_LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLeaves.CHECK_DECAY, false);

    protected static final IBlockState BIRCH_LOG = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.BIRCH);
    protected static final IBlockState BIRCH_LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.BIRCH).withProperty(BlockLeaves.CHECK_DECAY, false);

    protected static final WorldGenerator TAIGA_1 = new WorldGenTaiga1();
    protected static final WorldGenerator TAIGA_2 = new WorldGenTaiga2(false);

    protected static final int LAYER_GRASS = 0;
    protected static final int LAYER_DIRT = 1;
    protected static final int LAYER_PODZOL = 2;

    protected GenLayer coverSelector;
    protected GenLayer clearingSelector;

    public Forest(GlobType type) {
        super(type);
    }

    @Override
    protected void createLayers() {
        GenLayer cover = new SelectionSeedLayer(2, 1);
        cover = new GenLayerVoronoiZoom(1000, cover);
        cover = new ReplaceRandomLayer(Forest.LAYER_DIRT, Forest.LAYER_PODZOL, 4, 2000, cover);
        cover = new GenLayerFuzzyZoom(3000, cover);

        this.coverSelector = cover;
        this.coverSelector.initWorldGenSeed(this.seed);

        GenLayer clearing = new SelectWeightedLayer(2,
                new SelectWeightedLayer.Entry(0, 6),
                new SelectWeightedLayer.Entry(1, 4));
        clearing = new GenLayerVoronoiZoom(4000, clearing);
        clearing = new GenLayerFuzzyZoom(5000, clearing);
        clearing = new ReplaceRandomLayer(0, 2, 16, 6000, clearing);
        clearing = new ReplaceRandomLayer(1, 0, 10, 7000, clearing);
        clearing = new GenLayerVoronoiZoom(8000, clearing);
        clearing = new GenLayerFuzzyZoom(9000, clearing);

        this.clearingSelector = clearing;
        this.clearingSelector.initWorldGenSeed(this.seed);
    }

    @Override
    public void coverDecorate(GlobPrimer primer, Random random, int x, int z) {
        this.iterate(point -> {
            if (random.nextInt(4) == 0) {
                int y = this.heightBuffer[point.index];
                IBlockState state = primer.getBlockState(point.localX, y, point.localZ);
                if (state.getBlock() instanceof BlockLiquid) {
                    if (random.nextInt(16) == 0) {
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
