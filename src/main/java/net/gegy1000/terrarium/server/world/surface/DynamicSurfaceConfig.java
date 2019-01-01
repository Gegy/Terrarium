package net.gegy1000.terrarium.server.world.surface;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.surfacebuilder.SurfaceConfig;
import net.minecraft.world.gen.surfacebuilder.TernarySurfaceConfig;

import java.util.Random;

public abstract class DynamicSurfaceConfig implements SurfaceConfig {
    @Override
    public BlockState getTopMaterial() {
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public BlockState getUnderMaterial() {
        return Blocks.AIR.getDefaultState();
    }

    protected abstract TernarySurfaceConfig buildConfig(Random random, Chunk chunk, int x, int z, int y, double surfaceNoise);
}
