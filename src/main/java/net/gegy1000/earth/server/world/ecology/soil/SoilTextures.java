package net.gegy1000.earth.server.world.ecology.soil;

import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

import java.util.Random;

public final class SoilTextures {
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
    private static final IBlockState RED_SANDSTONE_BLOCK = Blocks.RED_SANDSTONE.getDefaultState();
    private static final IBlockState STONE_BLOCK = Blocks.STONE.getDefaultState();
    private static final IBlockState TERRACOTTA_BLOCK = Blocks.HARDENED_CLAY.getDefaultState();

    private static final NoiseGeneratorPerlin NOISE = new NoiseGeneratorPerlin(new Random(54321), 1);

    public static final SoilTexture GRASS = grass(GRASS_BLOCK);
    public static final SoilTexture PODZOL = grass(PODZOL_BLOCK);
    public static final SoilTexture CLAY = homogenous(CLAY_BLOCK);
    public static final SoilTexture COARSE_DIRT = grass(COARSE_DIRT_BLOCK);

    public static final SoilTexture DESERT_SAND = sand(SAND_BLOCK, SANDSTONE_BLOCK);
    public static final SoilTexture DESERT_RED_SAND = sand(RED_SAND_BLOCK, RED_SANDSTONE_BLOCK);

    public static final SoilTexture SAND = scatter(DESERT_SAND, GRASS, -0.5);
    public static final SoilTexture RED_SAND = scatter(DESERT_RED_SAND, GRASS, -0.5);

    public static final SoilTexture ROCK = homogenous(STONE_BLOCK);
    public static final SoilTexture SANDSTONE = homogenous(SANDSTONE_BLOCK);
    public static final SoilTexture SNOW = (random, pos, slope, depth) -> {
        if (slope >= 40) {
            return STONE_BLOCK;
        }
        return SNOW_BLOCK;
    };

    public static final SoilTexture MESA = new MesaSoilTexture(new Random(1521));

    public static final SoilTexture BEACH = homogenous(SAND_BLOCK);
    public static final SoilTexture RIVER_BED = binaryPatches(homogenous(DIRT_BLOCK), homogenous(CLAY_BLOCK), -0.5);
    public static final SoilTexture OCEAN_FLOOR = homogenous(GRAVEL_BLOCK);

    public static final SoilTexture GRASS_AND_DIRT = binaryPatches(GRASS, COARSE_DIRT, -0.2);
    public static final SoilTexture GRASS_AND_SAND = binaryPatches(GRASS, DESERT_SAND, -0.2);
    public static final SoilTexture GRASS_AND_PODZOL = binaryPatches(GRASS, PODZOL, -0.2);
    public static final SoilTexture GRASS_AND_CLAY = binaryPatches(GRASS, CLAY, -0.2);

    private static SoilTexture grass(IBlockState grassBlock) {
        return (random, pos, slope, depth) -> {
            if (slope >= 60) {
                return STONE_BLOCK;
            }
            return depth == 0 ? grassBlock : DIRT_BLOCK;
        };
    }

    private static SoilTexture sand(IBlockState sandBlock, IBlockState sandstoneBlock) {
        return (random, pos, slope, depth) -> {
            if (slope >= 60) {
                return STONE_BLOCK;
            } else if (slope >= 30) {
                return sandstoneBlock;
            }
            return sandBlock;
        };
    }

    private static SoilTexture homogenous(IBlockState block) {
        return (random, pos, slope, depth) -> block;
    }

    private static SoilTexture binaryPatches(SoilTexture a, SoilTexture b, double bias) {
        return (random, pos, slope, depth) -> {
            int x = pos.getX();
            int z = pos.getZ();
            double noise = NOISE.getValue(x / 24.0, z / 24.0);
            noise += (random.nextDouble() - random.nextDouble()) * 0.4;
            if (noise > bias) {
                return a.sample(random, pos, slope, depth);
            } else {
                return b.sample(random, pos, slope, depth);
            }
        };
    }

    private static SoilTexture scatter(SoilTexture a, SoilTexture b, double bias) {
        double remappedBias = (bias + 1.0) / 2.0;
        return (random, pos, slope, depth) -> {
            if (random.nextDouble() > remappedBias) {
                return a.sample(random, pos, slope, depth);
            } else {
                return b.sample(random, pos, slope, depth);
            }
        };
    }
}
