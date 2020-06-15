package net.gegy1000.terrarium.server.world.chunk.tracker;

import net.gegy1000.terrarium.Terrarium;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindFieldException;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ColumnTrackerAccess implements ChunkTrackerAccess {
    private static Field chunkMapEntriesField;

    static {
        try {
            chunkMapEntriesField = ObfuscationReflectionHelper.findField(PlayerChunkMap.class, "field_111193_e");
        } catch (UnableToFindFieldException e) {
            Terrarium.LOGGER.error("Failed to find chunk entries field", e);
        }
    }

    private final WorldServer world;

    public ColumnTrackerAccess(WorldServer world) {
        this.world = world;
    }

    @Override
    public LinkedHashSet<ChunkPos> getSortedQueuedColumns() {
        List<PlayerChunkMapEntry> entries = getEntries(this.world.getPlayerChunkMap());
        Collection<PlayerChunkMapEntry> queuedEntries = entries.stream()
                .filter(this::shouldQueue)
                .collect(Collectors.toList());

        LinkedHashSet<ChunkPos> sortedColumns = queuedEntries.stream()
                .sorted(Comparator.comparingDouble(PlayerChunkMapEntry::getClosestPlayerDistance))
                .map(PlayerChunkMapEntry::getPos)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // require surrounding chunks to be loaded for decoration and lighting
        for (PlayerChunkMapEntry entry : queuedEntries) {
            ChunkPos column = entry.getPos();
            for (int z = -5; z <= 5; z++) {
                for (int x = -5; x <= 5; x++) {
                    sortedColumns.add(new ChunkPos(column.x + x, column.z + z));
                }
            }
        }

        return sortedColumns;
    }

    private boolean shouldQueue(PlayerChunkMapEntry entry) {
        return entry.getChunk() == null && !SavedColumnTracker.isSaved(this.world, entry.getPos());
    }

    @SuppressWarnings("unchecked")
    private static List<PlayerChunkMapEntry> getEntries(PlayerChunkMap chunkTracker) {
        if (chunkMapEntriesField != null) {
            try {
                return (List<PlayerChunkMapEntry>) chunkMapEntriesField.get(chunkTracker);
            } catch (Exception e) {
                Terrarium.LOGGER.error("Failed to get player chunk entries", e);
            }
        }

        return Collections.emptyList();
    }
}
