package net.gegy1000.terrarium.server.world.cover;

import net.gegy1000.terrarium.server.world.feature.tree.GenerousDenseShrubGenerator;
import net.gegy1000.terrarium.server.world.feature.tree.GenerousPineGenerator;
import net.gegy1000.terrarium.server.world.feature.tree.GenerousTaigaGenerator;
import net.gegy1000.terrarium.server.world.feature.tree.SmallShrubGenerator;
import net.gegy1000.terrarium.server.world.feature.tree.TallShrubGenerator;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTileAccess;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

import java.util.Random;

public abstract class CoverGenerator<T extends CoverGenerationContext> {
    public static final int MOUNTAINOUS_SLOPE = 20;
    public static final int CLIFF_SLOPE = 70;
    public static final int EXTREME_CLIFF_SLOPE = 150;

    protected static final IBlockState GRASS = Blocks.GRASS.getDefaultState();
    protected static final IBlockState COARSE_DIRT = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
    protected static final IBlockState PODZOL = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL);
    protected static final IBlockState GRAVEL = Blocks.GRAVEL.getDefaultState();
    protected static final IBlockState SAND = Blocks.SAND.getDefaultState();
    protected static final IBlockState CLAY = Blocks.CLAY.getDefaultState();

    protected static final IBlockState COBBLESTONE = Blocks.COBBLESTONE.getDefaultState();
    protected static final IBlockState HARDENED_CLAY = Blocks.HARDENED_CLAY.getDefaultState();
    protected static final IBlockState SANDSTONE = Blocks.SANDSTONE.getDefaultState();

    protected static final IBlockState TALL_GRASS = Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS);
    protected static final IBlockState DEAD_BUSH = Blocks.DEADBUSH.getDefaultState();
    protected static final IBlockState DOUBLE_TALL_GRASS = Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.GRASS);
    protected static final IBlockState BUSH = Blocks.LEAVES.getDefaultState().withProperty(BlockLeaves.CHECK_DECAY, false).withProperty(BlockLeaves.DECAYABLE, false);

    protected static final IBlockState WATER = Blocks.WATER.getDefaultState();
    protected static final IBlockState LILYPAD = Blocks.WATERLILY.getDefaultState();

    protected static final IBlockState OAK_LOG = Blocks.LOG.getDefaultState();
    protected static final IBlockState OAK_LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockLeaves.CHECK_DECAY, false);

    protected static final IBlockState JUNGLE_LOG = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
    protected static final IBlockState JUNGLE_LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLeaves.CHECK_DECAY, false);

    protected static final IBlockState BIRCH_LOG = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.BIRCH);
    protected static final IBlockState BIRCH_LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.BIRCH).withProperty(BlockLeaves.CHECK_DECAY, false);

    protected static final IBlockState SPRUCE_LOG = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE);
    protected static final IBlockState SPRUCE_LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.SPRUCE).withProperty(BlockLeaves.CHECK_DECAY, false);

    protected static final IBlockState ACACIA_LOG = Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA);
    protected static final IBlockState ACACIA_LEAF = Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.ACACIA).withProperty(BlockLeaves.CHECK_DECAY, false);

    protected static final WorldGenerator PINE_TREE = new GenerousPineGenerator();
    protected static final WorldGenerator SPRUCE_TREE = new GenerousTaigaGenerator(false);

    protected static final WorldGenerator OAK_TALL_SHRUB = new TallShrubGenerator(OAK_LOG, OAK_LEAF);
    protected static final WorldGenerator JUNGLE_TALL_SHRUB = new TallShrubGenerator(JUNGLE_LOG, JUNGLE_LEAF);
    protected static final WorldGenerator ACACIA_TALL_SHRUB = new TallShrubGenerator(ACACIA_LOG, ACACIA_LEAF);

    protected static final WorldGenerator OAK_SMALL_SHRUB = new SmallShrubGenerator(OAK_LOG, OAK_LEAF);
    protected static final WorldGenerator JUNGLE_SMALL_SHRUB = new SmallShrubGenerator(JUNGLE_LOG, JUNGLE_LEAF);
    protected static final WorldGenerator BIRCH_SMALL_SHRUB = new SmallShrubGenerator(BIRCH_LOG, BIRCH_LEAF);
    protected static final WorldGenerator SPRUCE_SMALL_SHRUB = new SmallShrubGenerator(SPRUCE_LOG, SPRUCE_LEAF);
    protected static final WorldGenerator ACACIA_SMALL_SHRUB = new SmallShrubGenerator(ACACIA_LOG, ACACIA_LEAF);

    protected static final WorldGenerator OAK_DENSE_SHRUB = new GenerousDenseShrubGenerator(OAK_LOG, OAK_LEAF);
    protected static final WorldGenerator JUNGLE_DENSE_SHRUB = new GenerousDenseShrubGenerator(JUNGLE_LOG, JUNGLE_LEAF);

    protected final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    protected final T context;

    protected final CoverType<T> coverType;

    protected CoverGenerator(T context, CoverType<T> coverType) {
        this.context = context;
        this.coverType = coverType;
    }

    protected final void iterateChunk(PointConsumer handler) {
        CoverRasterTileAccess coverRaster = this.context.getCoverRaster();
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                if (coverRaster.get(localX, localZ) == this.coverType) {
                    handler.handlePoint(localX, localZ);
                }
            }
        }
    }

    protected final int range(Random random, int minimum, int maximum) {
        return random.nextInt((maximum - minimum) + 1) + minimum;
    }

    protected final int[] sampleChunk(GenLayer layer, int x, int z) {
        IntCache.resetIntCache();
        return layer.getInts(x, z, 16, 16);
    }

    protected interface PointConsumer {
        void handlePoint(int localX, int localZ);
    }
}
