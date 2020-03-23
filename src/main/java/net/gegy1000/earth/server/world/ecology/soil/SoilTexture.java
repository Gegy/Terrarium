package net.gegy1000.earth.server.world.ecology.soil;

import net.gegy1000.earth.server.world.cover.Cover;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockStainedHardenedClay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;

public final class SoilTexture {
    private static final IBlockState GRASS_BLOCK = Blocks.GRASS.getDefaultState();
    private static final IBlockState DIRT_BLOCK = Blocks.DIRT.getDefaultState();
    private static final IBlockState COARSE_DIRT_BLOCK = Blocks.DIRT.getDefaultState()
            .withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
    private static final IBlockState CLAY_BLOCK = Blocks.CLAY.getDefaultState();
    private static final IBlockState SAND_BLOCK = Blocks.SAND.getDefaultState();
    private static final IBlockState GRAVEL_BLOCK = Blocks.GRAVEL.getDefaultState();
    private static final IBlockState SNOW_BLOCK = Blocks.SNOW.getDefaultState();

    private static final IBlockState STONE_BLOCK = Blocks.STONE.getDefaultState();
    private static final IBlockState COBBLESTONE_BLOCK = Blocks.COBBLESTONE.getDefaultState();

    private static final IBlockState TERRACOTTA_BLOCK = Blocks.STAINED_HARDENED_CLAY.getDefaultState();

    private static final IBlockState GRAY_TERRACOTTA_BLOCK = Blocks.STAINED_HARDENED_CLAY.getDefaultState()
            .withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.GRAY);

    public static final SoilConfig DIRT = SoilConfig.binary(SoilLayer.uniform(GRASS_BLOCK), SoilLayer.uniform(DIRT_BLOCK));
    public static final SoilConfig COARSE_DIRT = SoilConfig.unary(SoilLayer.uniform(COARSE_DIRT_BLOCK));
    public static final SoilConfig CLAY = SoilConfig.unary(SoilLayer.uniform(CLAY_BLOCK));
    public static final SoilConfig SAND = SoilConfig.unary(SoilLayer.uniform(SAND_BLOCK));
    public static final SoilConfig DEEP_SNOW = SoilConfig.unary(SoilLayer.uniform(SNOW_BLOCK));
    public static final SoilConfig STONE = SoilConfig.unary(SoilLayer.uniform(STONE_BLOCK));

    private SoilTexture() {
    }

    public static SoilConfig select(
            SoilClass soilClass,
            int slope,
            Cover cover
    ) {
        // TODO: better ocean generation
        if (cover == Cover.WATER) return SAND;

        if (slope < 60) {
            // TODO: gravel, erosion based on slope & rainfall

            if (cover == Cover.PERMANENT_SNOW || soilClass.isIce()) {
                return DEEP_SNOW;
            }

            if (soilClass.isSandy()) {
                return SAND;
            }

            if (soilClass.isRock()) {
                return STONE;
            }

            return DIRT;
        } else {
            return STONE;
        }
    }
}
