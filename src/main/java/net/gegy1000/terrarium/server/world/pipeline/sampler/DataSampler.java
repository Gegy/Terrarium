package net.gegy1000.terrarium.server.world.pipeline.sampler;

import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;

public interface DataSampler<T> {
    T sample(GenerationSettings settings, int x, int z, int width, int height);

    default boolean shouldSample() {
        return true;
    }

    Class<T> getSamplerType();
}
