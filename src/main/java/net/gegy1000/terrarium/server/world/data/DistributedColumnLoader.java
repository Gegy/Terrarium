package net.gegy1000.terrarium.server.world.data;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public final class DistributedColumnLoader implements ColumnDataLoader {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setNameFormat("terrarium-data-worker")
                    .build()
    );

    private final Function<ChunkPos, ColumnData> generator;

    private final Map<ChunkPos, Work> unseenWorkMap = new HashMap<>();
    private final Map<ChunkPos, Work> activeWorkMap = new HashMap<>();

    private final Object workStateMutex = new Object();

    public DistributedColumnLoader(Function<ChunkPos, ColumnData> generator) {
        this.generator = generator;
    }

    @Override
    public CompletableFuture<ColumnData> getAsync(ChunkPos columnPos) {
        CompletableFuture<ColumnData> future = new CompletableFuture<>();

        Work work = new Work(columnPos, future);
        EXECUTOR.submit(() -> {
            try {
                this.startWork(work);
                ColumnData data = this.generate(columnPos);
                work.future.complete(data);
            } catch (Throwable t) {
                work.future.completeExceptionally(t);
            } finally {
                this.completeWork(work);
            }
        });

        synchronized (this.workStateMutex) {
            this.unseenWorkMap.put(columnPos, work);
        }

        return future;
    }

    @Override
    public ColumnData getNow(ChunkPos columnPos) {
        Work activeWork = null;
        synchronized (this.workStateMutex) {
            // cancel any work that we haven't started processing yet
            Work unseenWork = this.unseenWorkMap.remove(columnPos);
            if (unseenWork != null) {
                unseenWork.future.cancel(true);
            } else {
                activeWork = this.activeWorkMap.get(columnPos);
            }
        }

        // if we're already working on this column, wait for it
        if (activeWork != null) {
            return activeWork.future.join();
        }

        return this.generate(columnPos);
    }

    @Override
    public void close() {
        this.cancelAll();
    }

    private void cancelAll() {
        synchronized (this.workStateMutex) {
            for (Work work : this.activeWorkMap.values()) {
                work.future.cancel(true);
            }
            for (Work work : this.unseenWorkMap.values()) {
                work.future.cancel(true);
            }
        }
    }

    private ColumnData generate(ChunkPos columnPos) {
        return this.generator.apply(columnPos);
    }

    private void startWork(Work work) {
        synchronized (this.workStateMutex) {
            this.activeWorkMap.put(work.columnPos, work);
            this.unseenWorkMap.remove(work.columnPos);
        }
    }

    private void completeWork(Work work) {
        synchronized (this.workStateMutex) {
            this.activeWorkMap.remove(work.columnPos);
        }
    }

    private static class Work {
        final ChunkPos columnPos;
        final CompletableFuture<ColumnData> future;

        Work(ChunkPos columnPos, CompletableFuture<ColumnData> future) {
            this.columnPos = columnPos;
            this.future = future;
        }
    }
}
