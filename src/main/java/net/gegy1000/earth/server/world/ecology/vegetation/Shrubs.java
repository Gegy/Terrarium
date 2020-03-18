package net.gegy1000.earth.server.world.ecology.vegetation;

import net.gegy1000.earth.server.world.feature.TallShrubFeature;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public final class Shrubs {
    public static final float RADIUS = 1.5F;

    public static final Vegetation ACACIA = Vegetation.builder()
            .generator(VegetationGenerator.of(Generators.ACACIA))
            .growthIndicator(Trees.Indicators.ACACIA)
            .build();

    public static final Vegetation BIRCH = Vegetation.builder()
            .generator(VegetationGenerator.of(Generators.BIRCH))
            .growthIndicator(Trees.Indicators.BIRCH)
            .build();

    public static final Vegetation OAK = Vegetation.builder()
            .generator(VegetationGenerator.of(Generators.OAK))
            .growthIndicator(Trees.Indicators.OAK)
            .build();

    public static final Vegetation JUNGLE = Vegetation.builder()
            .generator(VegetationGenerator.of(Generators.JUNGLE))
            .growthIndicator(Trees.Indicators.JUNGLE_LIKE)
            .build();

    public static final Vegetation SPRUCE = Vegetation.builder()
            .generator(VegetationGenerator.of(Generators.SPRUCE))
            .growthIndicator(Trees.Indicators.SPRUCE)
            .build();

    static class Generators {
        static final TallShrubFeature ACACIA = tallShrub(BlockPlanks.EnumType.ACACIA);
        static final TallShrubFeature BIRCH = tallShrub(BlockPlanks.EnumType.BIRCH);
        static final TallShrubFeature OAK = tallShrub(BlockPlanks.EnumType.OAK);
        static final TallShrubFeature JUNGLE = tallShrub(BlockPlanks.EnumType.JUNGLE);
        static final TallShrubFeature SPRUCE = tallShrub(BlockPlanks.EnumType.SPRUCE);

        private static TallShrubFeature tallShrub(BlockPlanks.EnumType type) {
            return new TallShrubFeature(false, log(type), leaf(type));
        }

        private static IBlockState log(BlockPlanks.EnumType type) {
            if (type.getMetadata() < 4) {
                return Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, type);
            } else {
                return Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, type);
            }
        }

        private static IBlockState leaf(BlockPlanks.EnumType type) {
            if (type.getMetadata() < 4) {
                return Blocks.LEAVES.getDefaultState()
                        .withProperty(BlockOldLeaf.VARIANT, type)
                        .withProperty(BlockLeaves.CHECK_DECAY, false);
            } else {
                return Blocks.LEAVES2.getDefaultState()
                        .withProperty(BlockNewLeaf.VARIANT, type)
                        .withProperty(BlockLeaves.CHECK_DECAY, false);
            }
        }
    }
}
