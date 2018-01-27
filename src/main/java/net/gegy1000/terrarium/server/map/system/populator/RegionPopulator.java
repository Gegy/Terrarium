package net.gegy1000.terrarium.server.map.system.populator;

import net.gegy1000.terrarium.server.map.RegionTilePos;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

public interface RegionPopulator<T> {
    T populate(EarthGenerationSettings settings, RegionTilePos pos, Coordinate regionSize, int width, int height);
}
