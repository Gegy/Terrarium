package net.gegy1000.terrarium.server.world.pipeline.data;

import net.minecraft.util.math.ChunkPos;

import java.util.concurrent.CompletableFuture;

public interface ColumnDataLoader extends AutoCloseable {
    CompletableFuture<ColumnData> getAsync(ChunkPos columnPos);

    ColumnData get(ChunkPos columnPos);

    @Override
    default void close() {
    }
}
