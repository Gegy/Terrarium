package net.gegy1000.earth.server.world.soil.horizon;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

import java.util.Random;

public final class BinaryPatchedHorizonConfig implements SoilHorizonConfig {
    private static final NoiseGeneratorPerlin NOISE = new NoiseGeneratorPerlin(new Random(12345), 4);

    private final IBlockState first;
    private final IBlockState second;

    private BinaryPatchedHorizonConfig(IBlockState first, IBlockState second) {
        this.first = first;
        this.second = second;
    }

    public static SoilHorizonConfig of(IBlockState first, IBlockState second) {
        return new BinaryPatchedHorizonConfig(first, second);
    }

    public static SoilHorizonConfig of(Block first, Block second) {
        return new BinaryPatchedHorizonConfig(first.getDefaultState(), second.getDefaultState());
    }

    @Override
    public IBlockState getState(int x, int z, int depth, Random random) {
        double value = NOISE.getValue(x * 0.125, z * 0.125);
        return value > 0.0 ? this.first : this.second;
    }
}
