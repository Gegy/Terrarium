package net.gegy1000.earth.server.world.ecology.vegetation;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.ecology.GrowthIndicator;
import net.gegy1000.earth.server.world.ecology.maxent.MaxentGrowthIndicator;
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
    public static final float RADIUS = 3.0F;

    public static final Vegetation ACACIA = Vegetation.builder()
            .generator(VegetationGenerator.of(Generators.ACACIA))
            .growthIndicator(Indicators.ACACIA)
            .build();

    public static final Vegetation BIRCH = Vegetation.builder()
            .generator(VegetationGenerator.of(Generators.BIRCH))
            .growthIndicator(Indicators.BIRCH)
            .build();

    public static final Vegetation OAK = Vegetation.builder()
            .generator((world, random, pos) -> {
                if (random.nextInt(10) == 0) {
                    Generators.BIG_OAK.generate(world, random, pos);
                } else {
                    Generators.OAK.generate(world, random, pos);
                }
            })
            .growthIndicator(Indicators.OAK)
            .build();

    public static final Vegetation JUNGLE = Vegetation.builder()
            .generator(VegetationGenerator.of(Generators.JUNGLE))
            .growthIndicator(Indicators.JUNGLE_LIKE)
            .build();

    public static final Vegetation BIG_JUNGLE = Vegetation.builder()
            .generator(VegetationGenerator.of(Generators.BIG_JUNGLE))
            .growthIndicator(Indicators.JUNGLE_LIKE.pow(3.0))
            .build();

    public static final Vegetation SPRUCE = Vegetation.builder()
            .generator(VegetationGenerator.of(Generators.SPRUCE))
            .growthIndicator(Indicators.SPRUCE)
            .build();

    public static final Vegetation PINE = Vegetation.builder()
            .generator(VegetationGenerator.of(Generators.PINE))
            .growthIndicator(Indicators.PINE)
            .build();

    public static class Indicators {
        public static final GrowthIndicator ACACIA = maxentIndicator("acacia");
        public static final GrowthIndicator BIRCH = maxentIndicator("birch").pow(1.0 / 2.0);
        public static final GrowthIndicator OAK = maxentIndicator("oak");
        public static final GrowthIndicator JUNGLE_LIKE = maxentIndicator("jungle_like");
        public static final GrowthIndicator SPRUCE = maxentIndicator("spruce");
        public static final GrowthIndicator PINE = maxentIndicator("pine");

        private static GrowthIndicator maxentIndicator(String path) {
            return MaxentGrowthIndicator.tryParse(new ResourceLocation(TerrariumEarth.ID, "vegetation/models/trees/" + path + ".lambdas"))
                    .orElse(GrowthIndicator.no());
        }
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
