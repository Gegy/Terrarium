package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.terrarium.server.world.region.GenerationRegionHandler;
import net.minecraft.world.World;

public interface DecorationComposer {
    void composeDecoration(World world, GenerationRegionHandler regionHandler, int chunkX, int chunkZ);
}
