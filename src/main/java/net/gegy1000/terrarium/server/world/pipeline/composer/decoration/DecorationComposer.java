package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.terrarium.server.world.pipeline.composer.ChunkComposer;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.World;
import net.minecraft.world.gen.IChunkGenerator;

public interface DecorationComposer extends ChunkComposer {
    void composeDecoration(IChunkGenerator generator, World world, RegionGenerationHandler regionHandler, int chunkX, int chunkZ);
}
