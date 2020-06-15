package net.gegy1000.terrarium.server.world.chunk.tracker;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.core.server.CubeWatcher;
import io.github.opencubicchunks.cubicchunks.core.server.PlayerCubeMap;
import io.github.opencubicchunks.cubicchunks.core.util.WatchersSortingList;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class CubeTrackerAccess implements ChunkTrackerAccess {
    private static final LongSortedSet EMPTY = new LongLinkedOpenHashSet();

    private static Field cubesToGenerateField;

    static {
        try {
            cubesToGenerateField = PlayerCubeMap.class.getDeclaredField("cubesToGenerate");
            cubesToGenerateField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Terrarium.LOGGER.error("Failed to find cube to generate field", e);
        }
    }

    private final WorldServer world;

    public CubeTrackerAccess(WorldServer world) {
        this.world = world;
    }

    @Override
    public LongSortedSet getSortedQueuedColumns() {
        PlayerChunkMap chunkTracker = this.world.getPlayerChunkMap();
        if (chunkTracker instanceof PlayerCubeMap) {
            WatchersSortingList<CubeWatcher> cubesToGenerate = getCubesToGenerate((PlayerCubeMap) chunkTracker);
            if (cubesToGenerate == null) {
                return EMPTY;
            }

            LongSortedSet queuedColumns = new LongLinkedOpenHashSet();
            LongSortedSet bufferColumns = new LongLinkedOpenHashSet();

            // the cubesToGenerate list is already sorted
            for (CubeWatcher watcher : cubesToGenerate) {
                long key = ChunkPos.asLong(watcher.getX(), watcher.getZ());

                if (queuedColumns.contains(key)) continue;

                CubePos cubePos = new CubePos(watcher.getX(), watcher.getY(), watcher.getZ());
                boolean queued = !SavedCubeTracker.isSaved(this.world, cubePos);

                if (queued) {
                    queuedColumns.add(key);

                    for (int z = -5; z <= 5; z++) {
                        for (int x = -5; x <= 5; x++) {
                            bufferColumns.add(ChunkPos.asLong(x + watcher.getX(), z + watcher.getZ()));
                        }
                    }
                }
            }

            queuedColumns.addAll(bufferColumns);

            return queuedColumns;
        }

        return EMPTY;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static WatchersSortingList<CubeWatcher> getCubesToGenerate(PlayerCubeMap cubeTracker) {
        if (cubesToGenerateField != null) {
            try {
                return (WatchersSortingList<CubeWatcher>) cubesToGenerateField.get(cubeTracker);
            } catch (ReflectiveOperationException e) {
                Terrarium.LOGGER.error("Failed to get cubes to generate", e);
            }
        }
        return null;
    }
}
