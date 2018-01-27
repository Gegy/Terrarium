package net.gegy1000.terrarium.server.map.system.sampler;

import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

public interface DataSampler<T> {
    T sample(EarthGenerationSettings settings, int x, int z, int width, int height);

    default boolean shouldSample() {
        return true;
    }
}
