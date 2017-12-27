package net.gegy1000.terrarium.server.map.glob;

import net.gegy1000.terrarium.server.map.glob.generator.primer.GlobPrimer;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.layer.GenLayer;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class GlobGenerator {
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

    protected final GlobType type;

    private final IBlockState topBlock;
    private final IBlockState fillerBlock;

    protected BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    protected World world;
    public GlobType[] globBuffer;
    protected int[] heightBuffer;
    public IBlockState[] coverBuffer;
    public IBlockState[] fillerBuffer;

    protected long seed;

    protected GlobGenerator(GlobType type) {
        this.type = type;

        this.topBlock = type.getBiome().topBlock;
        this.fillerBlock = type.getBiome().fillerBlock;
    }

    public void initialize(World world, GlobType[] globBuffer, int[] heightBuffer, IBlockState[] coverBuffer, IBlockState[] fillerBuffer) {
        this.world = world;
        this.seed = world.getSeed();
        this.globBuffer = globBuffer;
        this.heightBuffer = heightBuffer;
        this.coverBuffer = coverBuffer;
        this.fillerBuffer = fillerBuffer;

        this.createLayers();
    }

    protected void createLayers() {
    }

    public void decorate(Random random, int x, int z) {
    }

    public void coverDecorate(GlobPrimer primer, Random random, int x, int z) {
    }

    public void getCover(Random random, int x, int z) {
        this.iterate(point -> this.coverBuffer[point.index] = this.getCoverAt(random, x + point.localX, z + point.localX));
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

    protected <T> T select(Random random, T... items) {
        return items[random.nextInt(items.length)];
    }

    protected int range(Random random, int minimum, int maximum) {
        return random.nextInt(maximum - minimum) + minimum;
    }

    protected int[] sampleChunk(GenLayer layer, int x, int z) {
        return layer.getInts(x, z, 16, 16);
    }

    protected int scatter(Random random, int coordinate, int range) {
        return coordinate + random.nextInt(range) - random.nextInt(range);
    }

    protected BlockPos scatterDecorate(Random random, int x, int z) {
        this.pos.setPos(x + random.nextInt(16), 0, z + random.nextInt(16));
        return this.pos;
    }

    protected void decorateScatter(Random random, int x, int z, int count, Consumer<BlockPos> decorator) {
        for (int i = 0; i < count; i++) {
            decorator.accept(this.world.getTopSolidOrLiquidBlock(this.scatterDecorate(random, x, z)));
        }
    }

    protected void decorateScatter(Random random, int[] layer, int x, int z, int count, Consumer<BlockPos> decorator) {
        for (int i = 0; i < count; i++) {
            BlockPos pos = this.scatterDecorate(random, x, z);
            if (layer[(pos.getX() - x) + (pos.getZ() - z) * 16] == 0) {
                decorator.accept(this.world.getTopSolidOrLiquidBlock(pos));
            }
        }
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

    public GlobType getType() {
        return this.type;
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
    }
}
