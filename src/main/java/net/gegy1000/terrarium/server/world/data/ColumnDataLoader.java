package net.gegy1000.terrarium.server.world.data;

import net.gegy1000.justnow.executor.CurrentThreadExecutor;
import net.gegy1000.justnow.executor.LocalExecutor;
import net.gegy1000.justnow.executor.TaskHandle;
import net.gegy1000.justnow.future.Future;
import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.Map;

public final class ColumnDataLoader implements AutoCloseable {
    private final DataGenerator generator;
    private final Map<ChunkPos, TaskHandle<ColumnData>> taskMap = new HashMap<>();

    private final LocalExecutor executor = new LocalExecutor();

    ColumnDataLoader(DataGenerator generator) {
        this.generator = generator;
    }

    public void advanceUntil(long endNanoTime) {
        while (System.nanoTime() < endNanoTime) {
            this.executor.advanceAll();
        }
    }

    public Future<ColumnData> spawn(ChunkPos columnPos) {
        TaskHandle<ColumnData> handle = this.executor.spawn(this.generate(columnPos));
        synchronized (this.taskMap) {
            this.taskMap.put(columnPos, handle);
        }
        return handle;
    }

    public ColumnData getNow(ChunkPos columnPos) {
        Future<ColumnData> future = this.stealTask(columnPos);
        if (future == null) {
            future = this.generate(columnPos);
        }

        return CurrentThreadExecutor.blockOn(future);
    }

    private Future<ColumnData> generate(ChunkPos columnPos) {
        return this.generator.generate(DataView.of(columnPos));
    }

    private Future<ColumnData> stealTask(ChunkPos columnPos) {
        TaskHandle<ColumnData> task;
        synchronized (this.taskMap) {
            task = this.taskMap.remove(columnPos);
        }

        return task != null ? this.executor.steal(task) : null;
    }

    public void cancel(ChunkPos columnPos) {
        synchronized (this.taskMap) {
            TaskHandle<ColumnData> handle = this.taskMap.remove(columnPos);
            if (handle != null) {
                this.executor.cancel(handle);
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
                this.executor.cancel(handle);
            }
            this.taskMap.clear();
        }
    }
}
