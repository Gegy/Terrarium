package net.gegy1000.terrarium.server.world.chunk.tracker;

import java.util.Collection;

public interface ChunkTrackerAccess {
    Collection<TrackedColumn> getSortedTrackedColumns();
}
