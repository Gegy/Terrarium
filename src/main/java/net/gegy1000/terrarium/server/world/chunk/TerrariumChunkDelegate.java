package net.gegy1000.terrarium.server.world.chunk;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;

public interface TerrariumChunkDelegate {
    Chunk generateChunk(int chunkX, int chunkZ);

    void populateTerrain(int chunkX, int chunkZ, ChunkPrimer primer);

    void populate(int chunkX, int chunkZ);
}
