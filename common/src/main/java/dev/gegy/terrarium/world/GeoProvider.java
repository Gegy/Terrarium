package dev.gegy.terrarium.world;

import dev.gegy.terrarium.backend.GeoChunk;
import dev.gegy.terrarium.backend.layer.GeoLayer;
import dev.gegy.terrarium.world.generator.GeoChunkCache;
import net.minecraft.world.level.ChunkPos;

import java.util.concurrent.CompletableFuture;

public class GeoProvider {
    private final GeoChunkCache cache;

    public GeoProvider(final GeoLayer<GeoChunk> layer) {
        cache = new GeoChunkCache(layer);
    }

    public CompletableFuture<GeoChunk> getOrLoad(final ChunkPos pos) {
        return cache.getOrLoad(pos);
    }

    public GeoChunk getOrLoadSync(final ChunkPos pos) {
        return getOrLoad(pos).join();
    }
}
