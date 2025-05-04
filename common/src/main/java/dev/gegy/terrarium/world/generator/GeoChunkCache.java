package dev.gegy.terrarium.world.generator;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.gegy.terrarium.backend.GeoChunk;
import dev.gegy.terrarium.backend.GeoView;
import dev.gegy.terrarium.backend.layer.GeoLayer;
import net.minecraft.world.level.ChunkPos;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class GeoChunkCache {
    private static final Duration EXPIRY_TIME = Duration.ofSeconds(10);
    private static final int MAXIMUM_SIZE = 32 * 32;

    private final LoadingCache<ChunkPos, CompletableFuture<GeoChunk>> cache;

    public GeoChunkCache(final GeoLayer<GeoChunk> layer) {
        cache = CacheBuilder.newBuilder()
                .maximumSize(MAXIMUM_SIZE)
                .expireAfterAccess(EXPIRY_TIME)
                .build(new CacheLoader<>() {
                    @Override
                    public CompletableFuture<GeoChunk> load(final ChunkPos pos) {
                        final GeoView view = new GeoView(pos.getMinBlockX(), pos.getMinBlockZ(), pos.getMaxBlockX(), pos.getMaxBlockZ());
                        return layer.getExact(view).thenApply(chunk -> chunk.orElse(GeoChunk.EMPTY));
                    }
                });
    }

    public CompletableFuture<GeoChunk> getOrLoad(final ChunkPos pos) {
        return cache.getUnchecked(pos);
    }
}
