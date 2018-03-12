package net.gegy1000.terrarium.server.world.pipeline.populator;

import net.gegy1000.terrarium.server.world.region.RegionTilePos;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;

public interface RegionPopulator<T> {
    T populate(GenerationSettings settings, RegionTilePos pos, Coordinate regionSize, int width, int height);

    Class<T> getType();
}
