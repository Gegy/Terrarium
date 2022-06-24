package dev.gegy.terrarium.world.generator.chunk.data;

import dev.gegy.terrarium.world.chunk.ChunkStatusDecorator;
import dev.gegy.terrarium.world.chunk.GeoChunkHolder;
import dev.gegy.terrarium.world.generator.chunk.GeoChunkGenerator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GeoChunkLoader {
    private static final CompletableFuture<?> DISABLED = CompletableFuture.completedFuture(null);

    public static void bootstrap() {
        ChunkStatusDecorator.runBefore(ChunkStatus.STRUCTURE_STARTS, GeoChunkLoader::loadGeoChunk);
    }

    private static CompletableFuture<?> loadGeoChunk(final ServerLevel level, final ChunkGenerator generator, final ChunkAccess chunk, final List<ChunkAccess> context) {
        if (generator instanceof final GeoChunkGenerator geoChunkGenerator) {
            return geoChunkGenerator.loadGeoChunk(chunk.getPos())
                .thenAccept(geoChunk -> GeoChunkHolder.put(chunk, geoChunk));
        }
        return DISABLED;
    }
}
