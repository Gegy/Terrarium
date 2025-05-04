package dev.gegy.terrarium.world;

import dev.gegy.terrarium.backend.GeoChunk;
import dev.gegy.terrarium.backend.GeoView;
import dev.gegy.terrarium.backend.layer.GeoLayer;
import dev.gegy.terrarium.backend.raster.RasterShape;
import dev.gegy.terrarium.world.generator.GeoChunkCache;
import net.minecraft.world.level.ChunkPos;

import java.util.concurrent.CompletableFuture;

public class GeoProvider {
    private final GeoLayer<GeoChunk> layer;
    private final GeoChunkCache cachedLayer;

    public GeoProvider(final GeoLayer<GeoChunk> layer) {
        this.layer = layer;
        cachedLayer = new GeoChunkCache(layer);
    }

    public CompletableFuture<GeoChunk> getOrLoad(final ChunkPos pos) {
        return cachedLayer.getOrLoad(pos);
    }

    public GeoChunk getOrLoadSync(final ChunkPos pos) {
        return getOrLoad(pos).join();
    }

    public CompletableFuture<GeoChunk> load(final GeoView sourceView, final RasterShape outputShape) {
        return layer.get(sourceView, outputShape).thenApply(chunk -> chunk.orElse(GeoChunk.EMPTY));
    }
}
