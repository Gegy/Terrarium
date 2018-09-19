package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.terrarium.server.world.chunk.populate.PopulateChunk;
import net.gegy1000.terrarium.server.world.pipeline.composer.ChunkComposer;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.World;

public interface DecorationComposer extends ChunkComposer {
    void composeDecoration(World world, RegionGenerationHandler regionHandler, PopulateChunk chunk);
}
