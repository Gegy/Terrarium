package net.gegy1000.terrarium.server.world.chunk.tracker;

import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindFieldException;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class ColumnTrackerAccess implements ChunkTrackerAccess {
    private static Field entriesWithoutChunksField;

    static {
        try {
            entriesWithoutChunksField = ObfuscationReflectionHelper.findField(PlayerChunkMap.class, "field_187311_h");
        } catch (UnableToFindFieldException e) {
            Terrarium.LOGGER.error("Failed to find entriesWithoutChunks field", e);
        }
    }

    private final WorldServer world;
    private final LongSortedSet bufferColumns = new LongLinkedOpenHashSet();

    public ColumnTrackerAccess(WorldServer world) {
        this.world = world;
    }

    @Override
    public LongSortedSet getSortedQueuedColumns() {
        LongSortedSet queuedColumns = new LongLinkedOpenHashSet();
        this.bufferColumns.clear();

        List<PlayerChunkMapEntry> entriesWithoutChunks = getEntriesWithoutChunks(this.world.getPlayerChunkMap());

        // the entries list already sorted
        for (PlayerChunkMapEntry entry : entriesWithoutChunks) {
            ChunkPos pos = entry.getPos();

            boolean queued = !SavedColumnTracker.isSaved(this.world, pos);

            if (queued) {
                queuedColumns.add(ChunkPos.asLong(pos.x, pos.z));

                // require surrounding chunks to be loaded for decoration and lighting
                for (int z = -5; z <= 5; z++) {
                    for (int x = -5; x <= 5; x++) {
                        this.bufferColumns.add(ChunkPos.asLong(x + pos.x, z + pos.z));
                    }
                }
            }
        }

        queuedColumns.addAll(this.bufferColumns);

        return queuedColumns;
    }

    @SuppressWarnings("unchecked")
    private static List<PlayerChunkMapEntry> getEntriesWithoutChunks(PlayerChunkMap chunkTracker) {
        if (entriesWithoutChunksField != null) {
            try {
                return (List<PlayerChunkMapEntry>) entriesWithoutChunksField.get(chunkTracker);
            } catch (ReflectiveOperationException e) {
                Terrarium.LOGGER.error("Failed to get entriesWithoutChunks", e);
            }
        }

        return Collections.emptyList();
    }
}
