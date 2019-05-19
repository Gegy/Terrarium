package net.gegy1000.terrarium.server.world.pipeline.data;

import net.gegy1000.cubicglue.CubicGlue;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.terrarium.server.world.chunk.tracker.ChunkTrackerAccess;
import net.gegy1000.terrarium.server.world.chunk.tracker.ColumnTrackerAccess;
import net.gegy1000.terrarium.server.world.chunk.tracker.CubeTrackerAccess;
import net.gegy1000.terrarium.server.world.chunk.tracker.FallbackTrackerAccess;
import net.gegy1000.terrarium.server.world.chunk.tracker.TrackedColumn;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO: PLAN
//  Get rid of generation regions: populate data at a per-chunk level
//  For adapters that need wider context, we follow a system such as population in MC
//  Not sure how this API should be structured. Possibly we replace all adapters that don't need this with the
//  "data layer" system. Then adapters operate when enough neighbours are loaded
//  -
//  **NOTE**: with this, we'll need to load chunk data an extra chunk out than what MC needs. so consider how to do this
public class ColumnDataCache implements AutoCloseable {
    private final Map<ChunkPos, ColumnDataEntry> entries = new HashMap<>();

    private final ColumnDataLoader loader;

    private final ChunkTrackerAccess chunkTrackerAccess;

    public ColumnDataCache(World world, ColumnDataGenerator generator) {
        this.loader = new DistributedColumnLoader(generator::generate);
        this.chunkTrackerAccess = createTrackerAccess(world);
    }

    private static ChunkTrackerAccess createTrackerAccess(World world) {
        if (!(world instanceof WorldServer)) {
            return FallbackTrackerAccess.INSTANCE;
        }
        if (CubicGlue.isCubic(world)) {
            return createCubeTracker((WorldServer) world);
        }
        return new ColumnTrackerAccess((WorldServer) world);
    }

    private static ChunkTrackerAccess createCubeTracker(WorldServer world) {
        return new CubeTrackerAccess(world);
    }

    private void setTrackedColumns(Collection<ChunkPos> columns) {
        Stream<ColumnDataEntry> untrackedEntries = this.entries.values().stream()
                .filter(entry -> !columns.contains(entry.getColumnPos()));
        untrackedEntries.forEach(ColumnDataEntry::untrack);

        Stream<ColumnDataEntry> trackedEntries = columns.stream()
                .map(this::getEntry);
        trackedEntries.forEach(ColumnDataEntry::track);
    }

    public void trackColumns() {
        Collection<TrackedColumn> columnEntries = this.chunkTrackerAccess.getSortedTrackedColumns();

        Collection<ChunkPos> requiredColumns = columnEntries.stream()
                .filter(TrackedColumn::isQueued)
                .map(TrackedColumn::getPos)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        this.setTrackedColumns(requiredColumns);
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

    public <T extends Data> Optional<T> joinData(CubicPos cubicPos, DataKey<T> key) {
        return this.joinData(new ChunkPos(cubicPos.getX(), cubicPos.getZ()), key);
    }

    public <T extends Data> Optional<T> joinData(ChunkPos columnPos, DataKey<T> key) {
        try (ColumnDataEntry.Handle handle = this.acquireEntry(columnPos)) {
            ColumnData columnData = handle.join();
            return columnData.get(key);
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
