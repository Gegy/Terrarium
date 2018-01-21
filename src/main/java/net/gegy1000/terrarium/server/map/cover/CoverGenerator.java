package net.gegy1000.terrarium.server.map.cover;

import net.gegy1000.terrarium.server.map.LatitudinalZone;
import net.gegy1000.terrarium.server.map.cover.generator.primer.GlobPrimer;
import net.gegy1000.terrarium.server.world.generator.tree.GenerousDenseShrubGenerator;
import net.gegy1000.terrarium.server.world.generator.tree.GenerousPineGenerator;
import net.gegy1000.terrarium.server.world.generator.tree.GenerousTaigaGenerator;
import net.gegy1000.terrarium.server.world.generator.tree.SmallShrubGenerator;
import net.gegy1000.terrarium.server.world.generator.tree.TallShrubGenerator;
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
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class CoverGenerator {
    protected static final IBlockState GRASS = Blocks.GRASS.getDefaultState();
    protected static final IBlockState COARSE_DIRT = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
    protected static final IBlockState PODZOL = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL);
    protected static final IBlockState GRAVEL = Blocks.GRAVEL.getDefaultState();
    protected static final IBlockState SAND = Blocks.SAND.getDefaultState();
    protected static final IBlockState CLAY = Blocks.CLAY.getDefaultState();

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

    protected final CoverType type;

    private final IBlockState topBlock;
    private final IBlockState fillerBlock;

    protected final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    protected World world;
    public CoverType[] globBuffer;
    protected int[] heightBuffer;
    public IBlockState[] coverBuffer;
    public IBlockState[] fillerBuffer;

    protected long seed;

    private final List<BlockPos> intersectionPoints = new ArrayList<>(16);
    private int intersectionRange = -1;

    protected CoverGenerator(CoverType type) {
        this.type = type;

        Biome defaultBiome = type.getDefaultBiome();
        this.topBlock = defaultBiome.topBlock;
        this.fillerBlock = defaultBiome.fillerBlock;
    }

    public void initialize(World world, CoverType[] globBuffer, int[] heightBuffer, IBlockState[] coverBuffer, IBlockState[] fillerBuffer, boolean debug) {
        this.world = world;
        this.seed = world.getSeed();
        this.globBuffer = globBuffer;
        this.heightBuffer = heightBuffer;
        this.coverBuffer = coverBuffer;
        this.fillerBuffer = fillerBuffer;

        this.createLayers(debug);
    }

    protected void createLayers(boolean debug) {
    }

    public void decorate(Random random, LatitudinalZone zone, int x, int z) {
    }

    public void coverDecorate(GlobPrimer primer, Random random, int x, int z) {
    }

    public void getCover(Random random, int x, int z) {
        this.iterate(point -> this.coverBuffer[point.index] = this.getCoverAt(random, x + point.localX, z + point.localZ));
    }

    protected IBlockState getCoverAt(Random random, int x, int z) {
        return this.topBlock;
    }

    public void getFiller(Random random, int x, int z) {
        this.iterate(point -> this.fillerBuffer[point.index] = this.getFillerAt(random, x + point.localX, z + point.localZ));
    }

    protected IBlockState getFillerAt(Random random, int x, int z) {
        return this.fillerBlock;
    }

    protected int range(Random random, int minimum, int maximum) {
        return random.nextInt((maximum - minimum) + 1) + minimum;
    }

    protected int[] sampleChunk(GenLayer layer, int x, int z) {
        IntCache.resetIntCache();
        return layer.getInts(x, z, 16, 16);
    }

    protected BlockPos scatterDecorate(Random random, int x, int z) {
        int scatterX = random.nextInt(16);
        int scatterZ = random.nextInt(16);
        this.pos.setPos(x + scatterX, 0, z + scatterZ);
        return this.pos;
    }

    protected void decorateScatter(Random random, int x, int z, int count, Consumer<BlockPos> decorator) {
        for (int i = 0; i < count; i++) {
            BlockPos pos = this.scatterDecorate(random, x, z);
            if (!this.tryPlace(pos)) {
                continue;
            }
            BlockPos topBlock = this.world.getTopSolidOrLiquidBlock(pos);
            if (!this.world.isAirBlock(topBlock)) {
                this.world.setBlockToAir(topBlock);
            }
            decorator.accept(topBlock);
        }
    }

    protected void decorateScatterSample(Random random, int x, int z, int count, Consumer<DecoratePoint> decorator) {
        for (int i = 0; i < count; i++) {
            BlockPos pos = this.scatterDecorate(random, x, z);
            if (!this.tryPlace(pos)) {
                continue;
            }
            ChunkPoint chunkPoint = new ChunkPoint(pos.getX() - x, pos.getZ() - z);
            BlockPos topBlock = this.world.getHeight(pos);
            if (!this.world.isAirBlock(topBlock)) {
                this.world.setBlockToAir(topBlock);
            }
            decorator.accept(new DecoratePoint(chunkPoint, topBlock));
        }
    }

    protected boolean tryPlace(BlockPos pos) {
        if (this.intersectionRange > 0) {
            if (this.checkHorizontalIntersection(pos)) {
                return false;
            }
            this.intersectionPoints.add(pos.toImmutable());
        }
        return true;
    }

    protected boolean checkHorizontalIntersection(BlockPos pos) {
        int range = this.intersectionRange;
        for (BlockPos intersectionPoint : this.intersectionPoints) {
            int deltaX = Math.abs(intersectionPoint.getX() - pos.getX());
            int deltaZ = Math.abs(intersectionPoint.getZ() - pos.getZ());
            if (deltaX <= range && deltaZ <= range) {
                return true;
            }
        }
        return false;
    }

    protected void preventIntersection(int range) {
        this.intersectionRange = range;
    }

    protected void stopIntersectionPrevention() {
        this.intersectionRange = -1;
        this.intersectionPoints.clear();
    }

    protected void coverLayer(IBlockState[] buffer, int x, int z, GenLayer layer, Function<Integer, IBlockState> populate) {
        int[] sampled = this.sampleChunk(layer, x, z);
        this.iterate(point -> buffer[point.index] = populate.apply(sampled[point.index]));
    }

    protected void iterate(Consumer<ChunkPoint> handlePoint) {
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int index = localX + localZ * 16;
                if (this.globBuffer[index] == this.type) {
                    handlePoint.accept(new ChunkPoint(localX, localZ, index));
                }
            }
        }
    }

    public CoverType getType() {
        return this.type;
    }

    public static class DecoratePoint {
        public final ChunkPoint chunk;
        public final BlockPos pos;

        public DecoratePoint(ChunkPoint chunk, BlockPos pos) {
            this.chunk = chunk;
            this.pos = pos;
        }
    }

    public static class ChunkPoint {
        public final int localX;
        public final int localZ;
        public final int index;

        public ChunkPoint(int localX, int localZ, int index) {
            this.localX = localX;
            this.localZ = localZ;
            this.index = index;
        }

        public ChunkPoint(int localX, int localZ) {
            this(localX, localZ, localX + localZ * 16);
        }
    }
}
