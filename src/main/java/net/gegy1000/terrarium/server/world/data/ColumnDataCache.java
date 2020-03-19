package net.gegy1000.terrarium.server.world.data;

import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.core.GenGen;
import net.gegy1000.terrarium.server.world.chunk.tracker.ChunkTrackerAccess;
import net.gegy1000.terrarium.server.world.chunk.tracker.ColumnTrackerAccess;
import net.gegy1000.terrarium.server.world.chunk.tracker.CubeTrackerAccess;
import net.gegy1000.terrarium.server.world.chunk.tracker.FallbackTrackerAccess;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ColumnDataCache implements AutoCloseable {
    private final Map<ChunkPos, ColumnDataEntry> entries = new HashMap<>();

    private final ColumnDataLoader loader;

    private final ChunkTrackerAccess chunkTrackerAccess;

    public ColumnDataCache(World world, DataGenerator generator) {
        this.loader = new DistributedColumnLoader(column -> generator.generate(DataView.of(column)));
        this.chunkTrackerAccess = createTrackerAccess(world);
    }

    private static ChunkTrackerAccess createTrackerAccess(World world) {
        if (!(world instanceof WorldServer)) {
            return FallbackTrackerAccess.INSTANCE;
        }
        if (GenGen.isCubic(world)) {
            return createCubeTracker((WorldServer) world);
        }
        return new ColumnTrackerAccess((WorldServer) world);
    }

    private static ChunkTrackerAccess createCubeTracker(WorldServer world) {
        return new CubeTrackerAccess(world);
    }

    private void setTrackedColumns(Collection<ChunkPos> columns) {
        for (ColumnDataEntry entry : this.entries.values()) {
            if (!columns.contains(entry.getColumnPos())) {
                entry.untrack();
            }
        }

        for (ChunkPos column : columns) {
            ColumnDataEntry entry = this.getEntry(column);
            entry.track();
        }
    }

    public void trackColumns() {
        this.setTrackedColumns(this.chunkTrackerAccess.getSortedQueuedColumns());
    }

    public void dropColumns() {
        List<ChunkPos> droppedColumns = this.entries.values().stream()
                .filter(ColumnDataEntry::tryDrop)
                .map(ColumnDataEntry::getColumnPos)
                .collect(Collectors.toList());

        droppedColumns.forEach(this.entries::remove);
    }

    public ColumnDataEntry.Handle acquireEntry(ChunkPos columnPos) {
        return this.getEntry(columnPos).acquire();
    }

    public <T> Optional<T> joinData(CubicPos cubicPos, DataKey<T> key) {
        return this.joinData(new ChunkPos(cubicPos.getX(), cubicPos.getZ()), key);
    }

    public <T> Optional<T> joinData(ChunkPos columnPos, DataKey<T> key) {
        try (ColumnDataEntry.Handle handle = this.acquireEntry(columnPos)) {
            ColumnData data = handle.join();
            return data.get(key);
        }
    }

    private ColumnDataEntry getEntry(ChunkPos columnPos) {
        return this.entries.computeIfAbsent(columnPos, pos -> new ColumnDataEntry(pos, this.loader));
    }

    @Override
    public void close() {
        this.loader.close();
    }
}
