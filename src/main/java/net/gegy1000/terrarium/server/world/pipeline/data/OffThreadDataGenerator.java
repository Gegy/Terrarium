package net.gegy1000.terrarium.server.world.pipeline.data;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.util.math.ChunkPos;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class OffThreadDataGenerator implements ColumnDataGenerator {
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder()
            .setNameFormat("terrarium-data-generator")
            .setDaemon(true)
            .build();

    private final DataEngine engine;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(THREAD_FACTORY);

    public OffThreadDataGenerator(DataEngine engine) {
        this.engine = engine;
    }

    @Override
    public CompletableFuture<ColumnData> get(ChunkPos columnPos) {
        return CompletableFuture.supplyAsync(() -> this.engine.populateData(columnPos), this.executor);
    }

    @Override
    public void close() {
        this.executor.shutdownNow();
    }
}
