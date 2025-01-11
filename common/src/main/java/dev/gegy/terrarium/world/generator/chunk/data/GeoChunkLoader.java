package dev.gegy.terrarium.world.generator.chunk.data;

import dev.gegy.terrarium.world.GeoProvider;
import dev.gegy.terrarium.world.GeoProviderHolder;
import dev.gegy.terrarium.world.chunk.GeoChunkHolder;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.WorldGenContext;

import java.util.concurrent.CompletableFuture;

public class GeoChunkLoader {
    private static final CompletableFuture<?> DISABLED = CompletableFuture.completedFuture(null);

    public static CompletableFuture<?> loadGeoChunk(final WorldGenContext context, final StaticCache2D<GenerationChunkHolder> chunkCache, final ChunkAccess chunk) {
        final GeoProvider geoProvider = GeoProviderHolder.get(context.level());
        if (geoProvider != null) {
            return geoProvider.getOrLoad(chunk.getPos())
                    .thenAccept(geoChunk -> GeoChunkHolder.put(chunk, geoChunk));
        }
        return DISABLED;
    }
}
