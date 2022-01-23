package net.gegy1000.terrarium.server.world.data;

import net.gegy1000.justnow.future.Future;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

public final class ColumnDataEntry {
    private static final long LEAK_TIME_THRESHOLD = 60 * 1000;

    private final ChunkPos columnPos;
    private final ColumnDataLoader loader;

    private final AtomicInteger handleCount = new AtomicInteger();
    private boolean tracked;

    private boolean dropped;

    @Nullable
    private Future<DataSample> future;
    private DataSample data;

    private long lastAccessTime = System.currentTimeMillis();

    ColumnDataEntry(ChunkPos columnPos, ColumnDataLoader loader) {
        this.columnPos = columnPos;
        this.loader = loader;
    }

    private void spawnIfNotLoaded() {
        this.touch();
        if (this.future == null && this.data == null) {
            this.future = this.loader.spawn(this.columnPos);
        }
    }

    void track() {
        if (!this.tracked) {
            this.tracked = true;
            this.spawnIfNotLoaded();
        }
    }

    void untrack() {
        this.tracked = false;
    }

    Handle acquire() {
        this.handleCount.getAndIncrement();
        this.spawnIfNotLoaded();
        return new Handle();
    }

    public ChunkPos getColumnPos() {
        return this.columnPos;
    }

    DataSample join() {
        this.touch();

        this.future = null;

        if (this.data != null) {
            return this.data;
        }

        this.data = this.loader.getNow(this.columnPos);

        return this.data;
    }

    private void touch() {
        if (this.dropped) throw new IllegalStateException("Cannot access dropped entry");

        this.lastAccessTime = System.currentTimeMillis();
    }

    private boolean checkLeaked() {
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

    private boolean shouldDrop() {
        return !this.tracked && this.handleCount.get() <= 0 || this.checkLeaked();
    }

    boolean tryDrop() {
        if (this.shouldDrop()) {
            this.dropped = true;
            this.loader.cancel(ChunkPos.asLong(this.columnPos.x, this.columnPos.z));
            this.future = null;
            this.data = null;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "ColumnDataEntry{handleCount=" + this.handleCount + ", tracked=" + this.tracked + "}";
    }

    public class Handle implements AutoCloseable {
        private boolean released;

        public DataSample join() {
            this.checkValid();
            return ColumnDataEntry.this.join();
        }

        public void release() {
            this.checkValid();
            ColumnDataEntry.this.handleCount.getAndDecrement();
            this.released = true;
        }

        public ChunkPos getColumnPos() {
            return ColumnDataEntry.this.columnPos;
        }

        @Override
        public void close() {
            this.release();
        }

        private void checkValid() {
            if (this.released) {
                throw new IllegalStateException("Handle has already been released!");
            }
        }
    }
}
