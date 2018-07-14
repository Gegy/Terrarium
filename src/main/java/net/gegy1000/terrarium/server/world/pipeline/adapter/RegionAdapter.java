package net.gegy1000.terrarium.server.world.pipeline.adapter;

import net.gegy1000.terrarium.server.world.region.RegionData;

public interface RegionAdapter {
    void adapt(RegionData data, int x, int z, int width, int height);
}
