package net.gegy1000.terrarium.server.world.chunk.tracker;

import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;

public class TrackedColumn {
    private final ChunkPos pos;
    private final boolean queued;

    public TrackedColumn(ChunkPos pos, boolean queued) {
        this.pos = pos;
        this.queued = queued;
    }

    public TrackedColumn(PlayerChunkMapEntry entry) {
        this(entry.getPos(), entry.getChunk() == null);
    }

    public ChunkPos getPos() {
        return this.pos;
    }

    public boolean isQueued() {
        return this.queued;
    }

    @Override
    public int hashCode() {
        return this.pos.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TrackedColumn && ((TrackedColumn) obj).pos.equals(this.pos);
    }
}
