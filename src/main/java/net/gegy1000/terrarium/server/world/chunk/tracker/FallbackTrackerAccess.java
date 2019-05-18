package net.gegy1000.terrarium.server.world.chunk.tracker;

import java.util.Collections;
import java.util.List;

public final class FallbackTrackerAccess implements ChunkTrackerAccess {
    public static final ChunkTrackerAccess INSTANCE = new FallbackTrackerAccess();

    private FallbackTrackerAccess() {
    }

    @Override
    public List<TrackedColumn> getSortedTrackedColumns() {
        return Collections.emptyList();
    }
}
