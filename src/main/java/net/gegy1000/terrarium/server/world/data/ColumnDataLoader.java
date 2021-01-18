package net.gegy1000.terrarium.server.world.data;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.gegy1000.justnow.executor.CurrentThreadExecutor;
import net.gegy1000.justnow.executor.LocalExecutor;
import net.gegy1000.justnow.executor.TaskHandle;
import net.gegy1000.justnow.future.Future;
import net.minecraft.util.math.ChunkPos;

public final class ColumnDataLoader implements AutoCloseable {
    private final DataGenerator generator;
    private final Long2ObjectMap<TaskHandle<ColumnData>> taskMap = new Long2ObjectOpenHashMap<>();

    private final LocalExecutor executor = new LocalExecutor();

    ColumnDataLoader(DataGenerator generator) {
        this.generator = generator;
    }

    public void advanceUntil(long endNanoTime) {
        while (System.nanoTime() < endNanoTime) {
            if (!this.executor.advanceAll()) {
                break;
            }
        }
    }

    public Future<ColumnData> spawn(ChunkPos columnPos) {
        TaskHandle<ColumnData> handle = this.executor.spawn(this.generate(columnPos));
        synchronized (this.taskMap) {
            this.taskMap.put(ChunkPos.asLong(columnPos.x, columnPos.z), handle);
        }
        return handle;
    }

    public ColumnData getNow(ChunkPos columnPos) {
        Future<ColumnData> future = this.stealTask(ChunkPos.asLong(columnPos.x, columnPos.z));
        if (future == null) {
            future = this.generate(columnPos);
        }

        return CurrentThreadExecutor.blockOn(future);
    }

    private Future<ColumnData> generate(ChunkPos columnPos) {
        return this.generator.generate(DataView.of(columnPos));
    }

    private Future<ColumnData> stealTask(long columnPos) {
        TaskHandle<ColumnData> task;
        synchronized (this.taskMap) {
            task = this.taskMap.remove(columnPos);
        }

        return task != null ? this.executor.steal(task) : null;
    }

    public void cancel(long columnPos) {
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
