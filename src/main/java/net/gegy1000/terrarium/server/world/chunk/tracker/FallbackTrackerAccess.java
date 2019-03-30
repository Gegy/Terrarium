package net.gegy1000.terrarium.server.world.chunk.tracker;

import java.util.Collections;
import java.util.List;

public class FallbackTrackerAccess implements ChunkTrackerAccess {
    @Override
    public List<TrackedColumn> getSortedTrackedColumns() {
        return Collections.emptyList();
    }
}
