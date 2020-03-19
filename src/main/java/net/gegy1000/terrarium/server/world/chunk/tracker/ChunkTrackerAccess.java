package net.gegy1000.terrarium.server.world.chunk.tracker;

import net.minecraft.util.math.ChunkPos;

import java.util.LinkedHashSet;

public interface ChunkTrackerAccess {
    LinkedHashSet<ChunkPos> getSortedQueuedColumns();
}
