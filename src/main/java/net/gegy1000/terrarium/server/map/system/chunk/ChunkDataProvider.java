package net.gegy1000.terrarium.server.map.system.chunk;

import net.gegy1000.terrarium.server.map.GenerationRegionHandler;
import net.minecraft.world.World;

public interface ChunkDataProvider<T> {
    void populate(GenerationRegionHandler regionHandler, World world, int originX, int originZ);

    T getResultStore();
}
