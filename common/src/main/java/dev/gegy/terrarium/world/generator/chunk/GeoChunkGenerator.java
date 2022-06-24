package dev.gegy.terrarium.world.generator.chunk;

import dev.gegy.terrarium.backend.GeoChunk;
import net.minecraft.world.level.ChunkPos;

import java.util.concurrent.CompletableFuture;

public interface GeoChunkGenerator {
    CompletableFuture<GeoChunk> loadGeoChunk(ChunkPos pos);
}
