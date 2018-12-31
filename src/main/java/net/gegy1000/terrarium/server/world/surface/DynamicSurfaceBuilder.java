package net.gegy1000.terrarium.server.world.surface;

import net.minecraft.block.BlockState;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.TernarySurfaceConfig;

import java.util.Random;

public abstract class DynamicSurfaceBuilder extends SurfaceBuilder<VoidSurfaceConfig> {
    public DynamicSurfaceBuilder() {
        super(VoidSurfaceConfig::deserialize);
    }

    @Override
    public void generate(Random random, Chunk chunk, Biome biome, int x, int z, int y, double surfaceNoise, BlockState defaultBlock, BlockState fluidBlock, int seaLevel, long seed, VoidSurfaceConfig config) {
        TernarySurfaceConfig dynamicConfig = this.buildConfig(random, chunk, x, z, y, surfaceNoise);
        SurfaceBuilder.DEFAULT.generate(random, chunk, biome, x, z, y, surfaceNoise, defaultBlock, fluidBlock, seaLevel, seed, dynamicConfig);
    }

    protected abstract TernarySurfaceConfig buildConfig(Random random, Chunk chunk, int x, int z, int y, double surfaceNoise);
}
