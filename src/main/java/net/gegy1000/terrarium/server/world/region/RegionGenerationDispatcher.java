package net.gegy1000.terrarium.server.world.region;

import java.util.Collection;

public interface RegionGenerationDispatcher {
    void setRequiredRegions(Collection<RegionTilePos> regions);

    Collection<GenerationRegion> collectCompletedRegions();

    GenerationRegion get(RegionTilePos pos);

    void close();
}
