package net.gegy1000.earth.server.world.ecology.vegetation;

import net.gegy1000.earth.server.world.feature.FloorShrubFeature;
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

    public static final Vegetation TALL_ACACIA = Vegetation.builder()
            .generator(Generators.TALL_ACACIA)
            .growthIndicator(Trees.Indicators.ACACIA)
            .build();

    public static final Vegetation TALL_BIRCH = Vegetation.builder()
            .generator(Generators.TALL_BIRCH)
            .growthIndicator(Trees.Indicators.BIRCH)
            .build();

    public static final Vegetation TALL_OAK = Vegetation.builder()
            .generator(Generators.TALL_OAK)
            .growthIndicator(Trees.Indicators.OAK)
            .build();

    public static final Vegetation TALL_JUNGLE = Vegetation.builder()
            .generator(Generators.TALL_JUNGLE)
            .growthIndicator(Trees.Indicators.JUNGLE_LIKE)
            .build();

    public static final Vegetation TALL_SPRUCE = Vegetation.builder()
            .generator(Generators.TALL_SPRUCE)
            .growthIndicator(Trees.Indicators.SPRUCE)
            .build();

    public static final Vegetation FLOOR_ACACIA = Vegetation.builder()
            .generator(Generators.FLOOR_ACACIA)
            .growthIndicator(Trees.Indicators.ACACIA)
            .build();

    public static final Vegetation FLOOR_BIRCH = Vegetation.builder()
            .generator(Generators.FLOOR_BIRCH)
            .growthIndicator(Trees.Indicators.BIRCH)
            .build();

    public static final Vegetation FLOOR_OAK = Vegetation.builder()
            .generator(Generators.FLOOR_OAK)
            .growthIndicator(Trees.Indicators.OAK)
            .build();

    public static final Vegetation FLOOR_JUNGLE = Vegetation.builder()
            .generator(Generators.FLOOR_JUNGLE)
            .growthIndicator(Trees.Indicators.JUNGLE_LIKE)
            .build();

    public static final Vegetation FLOOR_SPRUCE = Vegetation.builder()
            .generator(Generators.FLOOR_SPRUCE)
            .growthIndicator(Trees.Indicators.SPRUCE)
            .build();

    static class Generators {
        static final VegetationGenerator TALL_ACACIA = tallShrub(BlockPlanks.EnumType.ACACIA);
        static final VegetationGenerator TALL_BIRCH = tallShrub(BlockPlanks.EnumType.BIRCH);
        static final VegetationGenerator TALL_OAK = tallShrub(BlockPlanks.EnumType.OAK);
        static final VegetationGenerator TALL_JUNGLE = tallShrub(BlockPlanks.EnumType.JUNGLE);
        static final VegetationGenerator TALL_SPRUCE = tallShrub(BlockPlanks.EnumType.SPRUCE);

        static final VegetationGenerator FLOOR_ACACIA = floorShrub(BlockPlanks.EnumType.ACACIA);
        static final VegetationGenerator FLOOR_BIRCH = floorShrub(BlockPlanks.EnumType.BIRCH);
        static final VegetationGenerator FLOOR_OAK = floorShrub(BlockPlanks.EnumType.OAK);
        static final VegetationGenerator FLOOR_JUNGLE = floorShrub(BlockPlanks.EnumType.JUNGLE);
        static final VegetationGenerator FLOOR_SPRUCE = floorShrub(BlockPlanks.EnumType.SPRUCE);

        private static VegetationGenerator tallShrub(BlockPlanks.EnumType type) {
            return VegetationGenerator.of(new TallShrubFeature(false, log(type), leaf(type)));
        }

        private static VegetationGenerator floorShrub(BlockPlanks.EnumType type) {
            return VegetationGenerator.of(new FloorShrubFeature(false, log(type), leaf(type)));
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
