package net.gegy1000.earth.server.world.ecology.soil;

import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.geography.Landform;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public final class SoilTexture {
    private static final IBlockState GRASS_BLOCK = Blocks.GRASS.getDefaultState();
    private static final IBlockState DIRT_BLOCK = Blocks.DIRT.getDefaultState();
    private static final IBlockState COARSE_DIRT_BLOCK = Blocks.DIRT.getDefaultState()
            .withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
    private static final IBlockState PODZOL_BLOCK = Blocks.DIRT.getDefaultState()
            .withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL);
    private static final IBlockState CLAY_BLOCK = Blocks.CLAY.getDefaultState();
    private static final IBlockState SAND_BLOCK = Blocks.SAND.getDefaultState();
    private static final IBlockState RED_SAND_BLOCK = Blocks.SAND.getDefaultState()
            .withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND);
    private static final IBlockState GRAVEL_BLOCK = Blocks.GRAVEL.getDefaultState();
    private static final IBlockState SNOW_BLOCK = Blocks.SNOW.getDefaultState();
    private static final IBlockState SANDSTONE_BLOCK = Blocks.SANDSTONE.getDefaultState();
    private static final IBlockState STONE_BLOCK = Blocks.STONE.getDefaultState();
    private static final IBlockState TERRACOTTA_BLOCK = Blocks.HARDENED_CLAY.getDefaultState();

    public static final SoilConfig GRASSY = SoilConfig.binary(SoilLayer.uniform(GRASS_BLOCK), SoilLayer.uniform(DIRT_BLOCK));
    public static final SoilConfig BARE_DIRT = SoilConfig.unary(SoilLayer.uniform(COARSE_DIRT_BLOCK));
    public static final SoilConfig ROCKY = SoilConfig.unary(SoilLayer.uniform(STONE_BLOCK));
    public static final SoilConfig SNOWY = SoilConfig.unary(SoilLayer.uniform(SNOW_BLOCK));
    public static final SoilConfig SANDY = SoilConfig.unary(SoilLayer.uniform(SAND_BLOCK));

    public static final SoilConfig BEACH = SoilConfig.unary(SoilLayer.uniform(SAND_BLOCK));
    public static final SoilConfig OCEAN_BED = SoilConfig.unary(SoilLayer.uniform(SAND_BLOCK));
    public static final SoilConfig RIVER_BED = SoilConfig.unary(SoilLayer.uniform(DIRT_BLOCK));

    private SoilTexture() {
    }

    public static SoilConfig select(
            SoilSuborder soilSuborder,
            int slope,
            int organicCarbonContent,
            Cover cover,
            Landform landform
    ) {
        if (landform == Landform.SEA) return OCEAN_BED;
        if (landform == Landform.BEACH) return BEACH;
        if (landform == Landform.LAKE_OR_RIVER) return RIVER_BED;

        if (cover == Cover.PERMANENT_SNOW || soilSuborder == SoilSuborder.ICE) return SNOWY;
        if (soilSuborder == SoilSuborder.ROCK) return ROCKY;
        if (soilSuborder == SoilSuborder.SHIFTING_SAND) return SANDY;

        SoilConfig normal = GRASSY;
        SoilConfig hard = ROCKY;

        boolean hardened = slope >= 60 || cover == Cover.BARE_CONSOLIDATED;

        // TODO: in future use some form of vegetation index?
        boolean canGrowGrass = organicCarbonContent > 9;

        if (soilSuborder == SoilSuborder.PSAMMENTS) normal = SANDY;

        if (canGrowGrass) {
            if (normal == SANDY) normal = GRASSY;
        } else {
            if (normal == GRASSY) normal = BARE_DIRT;
        }

        return hardened ? hard : normal;
    }
}
