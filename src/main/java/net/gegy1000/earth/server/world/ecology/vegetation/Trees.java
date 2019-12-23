package net.gegy1000.earth.server.world.ecology.vegetation;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.ecology.GrowthIndicator;
import net.gegy1000.earth.server.world.ecology.maxent.MaxentGrowthIndicator;
import net.gegy1000.terrarium.server.util.Interpolate;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenBirchTree;
import net.minecraft.world.gen.feature.WorldGenMegaJungle;
import net.minecraft.world.gen.feature.WorldGenSavannaTree;
import net.minecraft.world.gen.feature.WorldGenTaiga1;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenTrees;

public final class Trees {
    public static final float RADIUS = 2.5F;

    public static final Vegetation ACACIA = Vegetation.builder()
            .generator((world, random, pos, indicator) -> Generators.ACACIA.generate(world, random, pos))
            .growthIndicator(maxentIndicator("acacia"))
            .build();

    public static final Vegetation BIRCH = Vegetation.builder()
            .generator((world, random, pos, indicator) -> Generators.BIRCH.generate(world, random, pos))
            .growthIndicator(maxentIndicator("birch"))
            .build();

    public static final Vegetation OAK = Vegetation.builder()
            .generator((world, random, pos, indicator) -> {
                if (random.nextInt(10) == 0) {
                    Generators.BIG_OAK.generate(world, random, pos);
                } else {
                    Generators.OAK.generate(world, random, pos);
                }
            })
            .growthIndicator(maxentIndicator("oak"))
            .build();

    public static final Vegetation JUNGLE = Vegetation.builder()
            .generator((world, random, pos, indicator) -> {
                double bigChance = 1.0 / (3 * Math.pow(1.0 / Interpolate.cosine(indicator), 3.0));
                if (random.nextFloat() <= bigChance) {
                    Generators.BIG_JUNGLE.generate(world, random, pos);
                } else {
                    Generators.JUNGLE.generate(world, random, pos);
                }
            })
            .growthIndicator(maxentIndicator("jungle_like"))
            .build();

    public static final Vegetation SPRUCE = Vegetation.builder()
            .generator((world, random, pos, indicator) -> Generators.SPRUCE.generate(world, random, pos))
            .growthIndicator(maxentIndicator("spruce"))
            .build();

    public static final Vegetation PINE = Vegetation.builder()
            .generator((world, random, pos, indicator) -> Generators.PINE.generate(world, random, pos))
            .growthIndicator(maxentIndicator("pine"))
            .build();

    private static GrowthIndicator maxentIndicator(String path) {
        return MaxentGrowthIndicator.tryParse(new ResourceLocation(TerrariumEarth.ID, "vegetation/models/trees/" + path + ".lambdas"))
                .orElse(GrowthIndicator.relaxed());
    }

    static class Generators {
        private static final IBlockState JUNGLE_LOG = Blocks.LOG.getDefaultState()
                .withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
        private static final IBlockState JUNGLE_LEAF = Blocks.LEAVES.getDefaultState()
                .withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE)
                .withProperty(BlockLeaves.CHECK_DECAY, Boolean.FALSE);

        static final WorldGenSavannaTree ACACIA = new WorldGenSavannaTree(false);
        static final WorldGenBirchTree BIRCH = new WorldGenBirchTree(false, false);
        static final WorldGenTrees OAK = new WorldGenTrees(false);
        static final WorldGenBigTree BIG_OAK = new WorldGenBigTree(false);
        static final WorldGenTrees JUNGLE = new WorldGenTrees(false, 7, JUNGLE_LOG, JUNGLE_LEAF, true);
        static final WorldGenMegaJungle BIG_JUNGLE = new WorldGenMegaJungle(false, 10, 20, JUNGLE_LOG, JUNGLE_LEAF);
        static final WorldGenTaiga1 PINE = new WorldGenTaiga1();
        static final WorldGenTaiga2 SPRUCE = new WorldGenTaiga2(false);
    }
}
