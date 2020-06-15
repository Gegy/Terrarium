package net.gegy1000.terrarium.server.world.data;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import net.gegy1000.gengen.core.GenGen;
import net.gegy1000.terrarium.server.util.UnpackChunkPos;
import net.gegy1000.terrarium.server.world.chunk.tracker.ChunkTrackerAccess;
import net.gegy1000.terrarium.server.world.chunk.tracker.ColumnTrackerAccess;
import net.gegy1000.terrarium.server.world.chunk.tracker.CubeTrackerAccess;
import net.gegy1000.terrarium.server.world.chunk.tracker.FallbackTrackerAccess;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ColumnDataCache implements AutoCloseable {
    private final Long2ObjectMap<ColumnDataEntry> entries = new Long2ObjectOpenHashMap<>();

    private final ColumnDataLoader loader;

    private final ChunkTrackerAccess chunkTrackerAccess;

    public ColumnDataCache(World world, DataGenerator dataGenerator) {
        this.loader = new ColumnDataLoader(dataGenerator);
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

    public void advanceUntil(long endNanoTime) {
        this.loader.advanceUntil(endNanoTime);
    }

    private void setTrackedColumns(LongSortedSet columns) {
        for (ColumnDataEntry entry : this.entries.values()) {
            ChunkPos pos = entry.getColumnPos();
            if (!columns.contains(ChunkPos.asLong(pos.x, pos.z))) {
                entry.untrack();
            }
        }

        LongIterator iterator = columns.iterator();
        while (iterator.hasNext()) {
            long key = iterator.nextLong();
            int x = UnpackChunkPos.unpackX(key);
            int z = UnpackChunkPos.unpackZ(key);

            ColumnDataEntry entry = this.getEntry(x, z);
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

        for (ChunkPos droppedColumn : droppedColumns) {
            this.entries.remove(ChunkPos.asLong(droppedColumn.x, droppedColumn.z));
        }
    }

    public ColumnDataEntry.Handle acquireEntry(int chunkX, int chunkZ) {
        return this.getEntry(chunkX, chunkZ).acquire();
    }

    public <T> Optional<T> joinData(int chunkX, int chunkZ, DataKey<T> key) {
        return this.joinData(chunkX, chunkZ).get(key);
    }

    public ColumnData joinData(int chunkX, int chunkZ) {
        return this.getEntry(chunkX, chunkZ).join();
    }

    private ColumnDataEntry getEntry(int chunkX, int chunkZ) {
        long key = ChunkPos.asLong(chunkX, chunkZ);

        ColumnDataEntry entry = this.entries.get(key);
        if (entry == null) {
            entry = new ColumnDataEntry(new ChunkPos(chunkX, chunkZ), this.loader);
            this.entries.put(key, entry);
        }

        return entry;
    }

    @Override
    public void close() {
        this.loader.close();
    }
}
