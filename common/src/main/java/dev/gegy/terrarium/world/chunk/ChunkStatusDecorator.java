package dev.gegy.terrarium.world.chunk;

import dev.gegy.terrarium.world.generator.chunk.data.GeoChunkLoader;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ChunkStatusDecorator {
    @Nullable
    public static Task getTaskToInject(final ChunkStatus status) {
        if (status == ChunkStatus.STRUCTURE_STARTS) {
            return GeoChunkLoader::loadGeoChunk;
        }
        return null;
    }

    public interface Task {
        CompletableFuture<?> run(WorldGenContext context, StaticCache2D<GenerationChunkHolder> chunkCache, ChunkAccess chunk);
    }
}
