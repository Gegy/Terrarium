package net.gegy1000.terrarium.server.world.chunk;

import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

public interface TerrariumChunkGenerator extends IChunkGenerator {
    void populateTerrain(int chunkX, int chunkZ, ChunkPrimer primer);
}
