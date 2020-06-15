package net.gegy1000.terrarium.server.world.chunk.tracker;

import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

public final class FallbackTrackerAccess implements ChunkTrackerAccess {
    public static final ChunkTrackerAccess INSTANCE = new FallbackTrackerAccess();

    private static final LongSortedSet EMPTY = new LongLinkedOpenHashSet();

    private FallbackTrackerAccess() {
    }

    @Override
    public LongSortedSet getSortedQueuedColumns() {
        return EMPTY;
    }
}
