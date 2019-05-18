package net.gegy1000.terrarium.server.world.pipeline.data;

import net.minecraft.util.math.ChunkPos;

import java.util.concurrent.CompletableFuture;

public interface ColumnDataGenerator extends AutoCloseable {
    CompletableFuture<ColumnData> get(ChunkPos columnPos);

    @Override
    void close();
}
