package net.gegy1000.terrarium.server.map.adapter;

import net.gegy1000.terrarium.server.map.RegionData;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

public interface RegionAdapter {
    void adapt(EarthGenerationSettings settings, RegionData data, int x, int z, int width, int height);
}
