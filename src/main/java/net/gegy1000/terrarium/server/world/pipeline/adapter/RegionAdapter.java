package net.gegy1000.terrarium.server.world.pipeline.adapter;

import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.region.RegionData;

public interface RegionAdapter {
    void adapt(GenerationSettings settings, RegionData data, int x, int z, int width, int height);
}
