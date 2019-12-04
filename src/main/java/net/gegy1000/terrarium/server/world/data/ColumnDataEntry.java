package net.gegy1000.terrarium.server.world.data;

import net.gegy1000.terrarium.Terrarium;
import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public final class ColumnDataEntry {
    private static final long LEAK_TIME_THRESHOLD = 60 * 1000;

    private final ChunkPos columnPos;
    private final ColumnDataLoader loader;

    private int acquireCount;
    private boolean tracked;

    private boolean dropped;

    @Nullable
    private CompletableFuture<ColumnData> future;

    private long lastAccessTime = System.currentTimeMillis();

    ColumnDataEntry(ChunkPos columnPos, ColumnDataLoader loader) {
        this.columnPos = columnPos;
        this.loader = loader;
    }

    private CompletableFuture<ColumnData> enqueue() {
        return this.loader.getAsync(this.columnPos);
    }

    void track() {
        if (!this.tracked) {
            this.tracked = true;
            this.future = this.enqueue();
        }
    }

    void untrack() {
        this.tracked = false;
    }

    public Handle acquire() {
        this.acquireCount++;
        if (this.future == null) {
            this.future = this.enqueue();
        }
        return new Handle();
    }

    public ChunkPos getColumnPos() {
        return this.columnPos;
    }

    CompletableFuture<ColumnData> future() {
        this.touch();
        if (this.future == null) {
            this.future = this.enqueue();
        }

        return this.future;
    }

    ColumnData join() {
        this.touch();
        if (this.future != null && this.future.isDone()) {
            return this.future.join();
        }

        ColumnData data = this.loader.getNow(this.columnPos);
        this.future = CompletableFuture.completedFuture(data);

        return data;
    }

    void touch() {
        if (this.dropped) throw new IllegalStateException("Cannot access dropped entry");

        this.lastAccessTime = System.currentTimeMillis();
    }

    boolean checkLeaked() {
        if (this.tracked) {
            return false;
        }
        long timeSinceAccess = System.currentTimeMillis() - this.lastAccessTime;
        if (timeSinceAccess > LEAK_TIME_THRESHOLD) {
            Terrarium.LOGGER.warn("Potential column data leak! {} not accessed in {}ms", this, timeSinceAccess);
            return true;
        }
        return false;
    }

    boolean shouldDrop() {
        return !this.tracked && this.acquireCount <= 0 || this.checkLeaked();
    }

    boolean tryDrop() {
        if (this.shouldDrop()) {
            this.dropped = true;
            if (this.future != null) {
                this.future.cancel(true);
                this.future = null;
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "ColumnDataEntry{acquireCount=" + this.acquireCount + ", tracked=" + this.tracked + "}";
    }

    public class Handle implements AutoCloseable {
        private boolean released;

        public CompletableFuture<ColumnData> future() {
            return ColumnDataEntry.this.future();
        }

        public ColumnData join() {
            return ColumnDataEntry.this.join();
        }

        public void release() {
            if (this.released) {
                throw new IllegalStateException("Handle has already been released!");
            }
            ColumnDataEntry.this.acquireCount--;
            this.released = true;
        }

        public ChunkPos getColumnPos() {
            return ColumnDataEntry.this.columnPos;
        }

        @Override
        public void close() {
            this.release();
        }
    }
}
