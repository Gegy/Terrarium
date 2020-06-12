package net.gegy1000.earth.server.world.ecology.soil;

import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

import java.util.Arrays;
import java.util.Random;

public final class MesaSoilTexture implements SoilTexture {
    private static final int BAND_COUNT = 64;
    private static final double NOISE_FREQUENCY = 1.0 / 512.0;

    private static final IBlockState TERRACOTTA = Blocks.HARDENED_CLAY.getDefaultState();
    private static final IBlockState COLORED_TERRACOTTA = Blocks.STAINED_HARDENED_CLAY.getDefaultState();
    private static final IBlockState ORANGE_TERRACOTTA = COLORED_TERRACOTTA.withProperty(BlockColored.COLOR, EnumDyeColor.ORANGE);

    private final IBlockState[] bands;
    private final NoiseGeneratorPerlin noise;

    public MesaSoilTexture(Random random) {
        this.bands = generateBands(random);
        this.noise = new NoiseGeneratorPerlin(random, 1);
    }

    private static IBlockState[] generateBands(Random random) {
        IBlockState[] bands = new IBlockState[BAND_COUNT];
        Arrays.fill(bands, TERRACOTTA);

        for (int i = 0; i < 64; ++i) {
            i += random.nextInt(5) + 1;
            if (i < 64) {
                bands[i] = ORANGE_TERRACOTTA;
            }
        }

        addSingleBands(bands, random, 1, EnumDyeColor.YELLOW);
        addSingleBands(bands, random, 2, EnumDyeColor.BROWN);
        addSingleBands(bands, random, 1, EnumDyeColor.RED);
        addGradientBands(bands, random, EnumDyeColor.WHITE, EnumDyeColor.SILVER);

        return bands;
    }

    private static void addSingleBands(IBlockState[] bands, Random random, int minDepth, EnumDyeColor color) {
        int count = random.nextInt(4) + 2;

        for (int i = 0; i < count; i++) {
            int depth = random.nextInt(3) + minDepth;
            int start = random.nextInt(BAND_COUNT);

            for (int y = 0; start + y < BAND_COUNT && y < depth; y++) {
                bands[start + y] = COLORED_TERRACOTTA.withProperty(BlockColored.COLOR, color);
            }
        }
    }

    private static void addGradientBands(IBlockState[] bands, Random random, EnumDyeColor main, EnumDyeColor fade) {
        int count = random.nextInt(3) + 3;

        int y = 0;
        for (int i = 0; i < count; i++) {
            y += random.nextInt(16) + 4;
            if (y >= BAND_COUNT) break;

            bands[y] = COLORED_TERRACOTTA.withProperty(BlockColored.COLOR, main);

            if (y > 1 && random.nextBoolean()) {
                bands[y - 1] = COLORED_TERRACOTTA.withProperty(BlockColored.COLOR, fade);
            }

            if (y < BAND_COUNT - 1 && random.nextBoolean()) {
                bands[y + 1] = COLORED_TERRACOTTA.withProperty(BlockColored.COLOR, fade);
            }
        }
    }

    @Override
    public IBlockState sample(Random random, BlockPos pos, int slope, int depth) {
        double noise = this.noise.getValue(pos.getX() * NOISE_FREQUENCY, pos.getZ() * NOISE_FREQUENCY);
        int offset = (int) Math.round(noise * 2.0);
        return this.bands[(pos.getY() + offset + BAND_COUNT) % BAND_COUNT];
    }
}
