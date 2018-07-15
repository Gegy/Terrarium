package net.gegy1000.terrarium.server.world.pipeline.composer;

import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;

public interface ChunkComposer {
    RegionComponentType<?>[] getDependencies();
}
