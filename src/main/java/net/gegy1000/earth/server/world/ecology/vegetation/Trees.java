package net.gegy1000.earth.server.world.ecology.vegetation;

import net.gegy1000.earth.server.world.ecology.GrowthIndicator;
import net.gegy1000.terrarium.server.util.WeightedPool;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.feature.WorldGenBirchTree;
import net.minecraft.world.gen.feature.WorldGenCanopyTree;
import net.minecraft.world.gen.feature.WorldGenMegaJungle;
import net.minecraft.world.gen.feature.WorldGenMegaPineTree;
import net.minecraft.world.gen.feature.WorldGenSavannaTree;
import net.minecraft.world.gen.feature.WorldGenTaiga1;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraft.world.gen.feature.WorldGenerator;

public final class Trees {
    public static final float RADIUS = 2.0F;

    private static final IBlockState OAK_LOG = Blocks.LOG.getDefaultState()
            .withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK);

    private static final IBlockState OAK_LEAF = Blocks.LEAVES.getDefaultState()
            .withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK)
            .withProperty(BlockLeaves.CHECK_DECAY, false);

    private static final IBlockState JUNGLE_LOG = Blocks.LOG.getDefaultState()
            .withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);

    private static final IBlockState JUNGLE_LEAF = Blocks.LEAVES.getDefaultState()
            .withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE)
            .withProperty(BlockLeaves.CHECK_DECAY, false);

    private static final WorldGenerator OAK_GENERATOR = new WorldGenTrees(false);
    private static final WorldGenerator BIRCH_GENERATOR = new WorldGenBirchTree(false, false);
    private static final WorldGenerator ACACIA_GENERATOR = new WorldGenSavannaTree(false);
    private static final WorldGenerator PINE_GENERATOR = new WorldGenTaiga1();
    private static final WorldGenerator SPRUCE_GENERATOR = new WorldGenTaiga2(false);
    private static final WorldGenerator REDWOOD_GENERATOR = new WorldGenMegaPineTree(false, false);
    private static final WorldGenerator DARK_OAK_GENERATOR = new WorldGenCanopyTree(false);
    private static final WorldGenerator WILLOW_TREE_GENERATOR = new WorldGenTrees(false, 4, OAK_LOG, OAK_LEAF, true);

    // TODO: GrowthIndicator
    public static final Vegetation OAK = Vegetation.builder()
            .withGrowthIndicator(GrowthIndicator.anywhere())
            .withGenerator(OAK_GENERATOR::generate)
            .build();

    public static final Vegetation BIRCH = Vegetation.builder()
            .withGrowthIndicator(GrowthIndicator.anywhere())
            .withGenerator(BIRCH_GENERATOR::generate)
            .build();

    public static final Vegetation ACACIA = Vegetation.builder()
            .withGrowthIndicator(GrowthIndicator.anywhere())
            .withGenerator(ACACIA_GENERATOR::generate)
            .build();

    public static final Vegetation PINE = Vegetation.builder()
            .withGrowthIndicator(GrowthIndicator.anywhere())
            .withGenerator(PINE_GENERATOR::generate)
            .build();

    public static final Vegetation SPRUCE = Vegetation.builder()
            .withGrowthIndicator(GrowthIndicator.anywhere())
            .withGenerator(SPRUCE_GENERATOR::generate)
            .build();

    public static final Vegetation REDWOOD = Vegetation.builder()
            .withGrowthIndicator(GrowthIndicator.anywhere())
            .withGenerator(REDWOOD_GENERATOR::generate)
            .build();

    public static final Vegetation DARK_OAK = Vegetation.builder()
            .withGrowthIndicator(GrowthIndicator.anywhere())
            .withGenerator(DARK_OAK_GENERATOR::generate)
            .build();

    public static final Vegetation THIN_JUNGLE = Vegetation.builder()
            .withGrowthIndicator(GrowthIndicator.anywhere())
            .withGenerator((world, random, pos) -> {
                WorldGenTrees generator = new WorldGenTrees(false, 4 + random.nextInt(7), JUNGLE_LOG, JUNGLE_LEAF, true);
                generator.generate(world, random, pos);
            })
            .build();

    public static final Vegetation THICK_JUNGLE = Vegetation.builder()
            .withGrowthIndicator(GrowthIndicator.anywhere())
            .withGenerator((world, random, pos) -> {
                WorldGenMegaJungle generator = new WorldGenMegaJungle(false, 10, 20, JUNGLE_LOG, JUNGLE_LEAF);
                generator.generate(world, random, pos);
            })
            .build();

    public static final Vegetation WILLOW = Vegetation.builder()
            .withGrowthIndicator(GrowthIndicator.anywhere())
            .withGenerator(WILLOW_TREE_GENERATOR::generate)
            .build();

    // TODO: Palm, dark oak, willow & redwood

    public static final WeightedPool<Vegetation> BROADLEAVED_DECIDUOUS = WeightedPool.<Vegetation>builder()
            .with(OAK, 10.0F)
            .with(ACACIA, 5.0F)
            .build();

    public static final WeightedPool<Vegetation> BROADLEAVED_EVERGREEN = WeightedPool.<Vegetation>builder()
            .with(THIN_JUNGLE, 10.0F)
            .with(THICK_JUNGLE, 5.0F)
            .build();

    public static final WeightedPool<Vegetation> NEEDLELEAVED_DECIDUOUS = WeightedPool.<Vegetation>builder()
            .with(BIRCH, 10.0F)
            .with(ACACIA, 5.0F)
            .build();

    public static final WeightedPool<Vegetation> NEEDLELEAVED_EVERGREEN = WeightedPool.<Vegetation>builder()
            .with(PINE, 5.0F)
            .with(SPRUCE, 10.0F)
            .build();
}
