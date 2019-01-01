package net.gegy1000.earth.server.world.surface;

import net.gegy1000.terrarium.server.world.surface.DynamicSurfaceConfig;
import net.minecraft.world.biome.layer.CachingLayerSampler;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.surfacebuilder.TernarySurfaceConfig;

import java.util.Random;
import java.util.function.IntFunction;

public class LayerSurfaceConfig extends DynamicSurfaceConfig {
    private final CachingLayerSampler sampler;
    private final IntFunction<TernarySurfaceConfig> surfaceSelector;

    public LayerSurfaceConfig(CachingLayerSampler sampler, IntFunction<TernarySurfaceConfig> surfaceSelector) {
        this.sampler = sampler;
        this.surfaceSelector = surfaceSelector;
    }

    @Override
    protected TernarySurfaceConfig buildConfig(Random random, Chunk chunk, int x, int z, int y, double surfaceNoise) {
        int value = this.sampler.sample(x, z);
        return this.surfaceSelector.apply(value);
    }
}
