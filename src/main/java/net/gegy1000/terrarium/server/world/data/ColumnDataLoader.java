package net.gegy1000.terrarium.server.world.data;

import net.gegy1000.justnow.executor.CurrentThreadExecutor;
import net.gegy1000.justnow.executor.LocalExecutor;
import net.gegy1000.justnow.executor.TaskHandle;
import net.gegy1000.justnow.future.Future;
import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class ColumnDataLoader implements AutoCloseable {
    private final LocalExecutor executor = new LocalExecutor();

    private final Function<ChunkPos, Future<ColumnData>> generator;

    private final Map<ChunkPos, TaskHandle<ColumnData>> taskMap = new HashMap<>();

    ColumnDataLoader(Function<ChunkPos, Future<ColumnData>> generator) {
        this.generator = generator;
    }

    public void advance() {
        this.executor.advanceAll();
    }

    public Future<ColumnData> spawn(ChunkPos columnPos) {
        TaskHandle<ColumnData> handle = this.executor.spawn(this.generator.apply(columnPos));
        synchronized (this.taskMap) {
            this.taskMap.put(columnPos, handle);
        }
        return handle;
    }

    public ColumnData getNow(ChunkPos columnPos) {
        TaskHandle<ColumnData> handle;
        synchronized (this.taskMap) {
            handle = this.taskMap.remove(columnPos);
        }

        Future<ColumnData> future;
        if (handle != null) {
            future = this.executor.steal(handle);
        } else {
            future = this.generator.apply(columnPos);
        }

        return CurrentThreadExecutor.blockOn(future);
    }

    public void cancel(ChunkPos columnPos) {
        synchronized (this.taskMap) {
            TaskHandle<ColumnData> handle = this.taskMap.remove(columnPos);
            if (handle != null) {
                this.executor.remove(handle);
            }
        }
    }

    @Override
    public void close() {
        this.cancelAll();
    }

    private void cancelAll() {
        synchronized (this.taskMap) {
            for (TaskHandle<ColumnData> handle : this.taskMap.values()) {
                this.executor.remove(handle);
            }
            this.taskMap.clear();
        }
    }
}
