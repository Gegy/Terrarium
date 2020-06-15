package net.gegy1000.terrarium.server.world.chunk.tracker;

import it.unimi.dsi.fastutil.longs.LongSortedSet;

public interface ChunkTrackerAccess {
    LongSortedSet getSortedQueuedColumns();
}
