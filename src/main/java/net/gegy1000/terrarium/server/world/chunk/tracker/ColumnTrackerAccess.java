package net.gegy1000.terrarium.server.world.chunk.tracker;

import net.gegy1000.terrarium.Terrarium;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ColumnTrackerAccess implements ChunkTrackerAccess {
    private static Field chunkMapEntriesField;

    static {
        try {
            chunkMapEntriesField = ReflectionHelper.findField(PlayerChunkMap.class, "entries", "field_111193_e");
        } catch (ReflectionHelper.UnableToFindFieldException e) {
            Terrarium.LOGGER.error("Failed to find chunk entries field", e);
        }
    }

    private final WorldServer world;

    public ColumnTrackerAccess(WorldServer world) {
        this.world = world;
    }

    @Override
    public List<TrackedColumn> getSortedTrackedColumns() {
        List<PlayerChunkMapEntry> entries = getSortedChunkEntries(this.world.getPlayerChunkMap());

        return entries.stream()
                .map(entry -> new TrackedColumn(entry.getPos(), this.shouldQueue(entry)))
                .collect(Collectors.toList());
    }

    private boolean shouldQueue(PlayerChunkMapEntry entry) {
        return entry.getChunk() == null && !SavedColumnTracker.isSaved(this.world, entry.getPos());
    }

    @SuppressWarnings("unchecked")
    public static List<PlayerChunkMapEntry> getEntries(PlayerChunkMap chunkTracker) {
        if (chunkMapEntriesField != null) {
            try {
                return (List<PlayerChunkMapEntry>) chunkMapEntriesField.get(chunkTracker);
            } catch (Exception e) {
                Terrarium.LOGGER.error("Failed to get player chunk entries", e);
            }
        }

        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private static List<PlayerChunkMapEntry> getSortedChunkEntries(PlayerChunkMap chunkTracker) {
        List<PlayerChunkMapEntry> entries = getEntries(chunkTracker);

        List<PlayerChunkMapEntry> sortedEntries = new ArrayList<>(entries);
        sortedEntries.sort(Comparator.comparingDouble(PlayerChunkMapEntry::getClosestPlayerDistance));

        return sortedEntries;
    }
}
