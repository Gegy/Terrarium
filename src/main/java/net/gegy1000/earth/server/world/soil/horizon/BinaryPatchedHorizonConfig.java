package net.gegy1000.earth.server.world.soil.horizon;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

import java.util.Random;

public final class BinaryPatchedHorizonConfig implements SoilHorizonConfig {
    private static final NoiseGeneratorPerlin NOISE = new NoiseGeneratorPerlin(new Random(12345), 4);

    private final IBlockState first;
    private final IBlockState second;

    private final double threshold;

    private BinaryPatchedHorizonConfig(IBlockState first, IBlockState second, double threshold) {
        this.first = first;
        this.second = second;
        this.threshold = threshold;
    }

    public static SoilHorizonConfig of(IBlockState first, IBlockState second, double threshold) {
        return new BinaryPatchedHorizonConfig(first, second, threshold);
    }

    public static SoilHorizonConfig of(IBlockState first, IBlockState second) {
        return new BinaryPatchedHorizonConfig(first, second, 0.0);
    }

    public static SoilHorizonConfig of(Block first, Block second, double threshold) {
        return new BinaryPatchedHorizonConfig(first.getDefaultState(), second.getDefaultState(), threshold);
    }

    public static SoilHorizonConfig of(Block first, Block second) {
        return new BinaryPatchedHorizonConfig(first.getDefaultState(), second.getDefaultState(), 0.0);
    }

    @Override
    public IBlockState getState(int x, int z, int depth, Random random) {
        double value = NOISE.getValue(x * 0.25, z * 0.25);
        return value > this.threshold ? this.first : this.second;
    }
}
