package net.gegy1000.terrarium.server.world.chunk.tracker;

import net.minecraft.util.math.ChunkPos;

import java.util.LinkedHashSet;

public final class FallbackTrackerAccess implements ChunkTrackerAccess {
    public static final ChunkTrackerAccess INSTANCE = new FallbackTrackerAccess();

    private static final LinkedHashSet<ChunkPos> EMPTY = new LinkedHashSet<>();

    private FallbackTrackerAccess() {
    }

    @Override
    public LinkedHashSet<ChunkPos> getSortedQueuedColumns() {
        return EMPTY;
    }
}
