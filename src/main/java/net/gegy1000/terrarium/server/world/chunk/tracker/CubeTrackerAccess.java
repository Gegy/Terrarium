package net.gegy1000.terrarium.server.world.chunk.tracker;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.util.XYZMap;
import io.github.opencubicchunks.cubicchunks.core.server.CubeWatcher;
import io.github.opencubicchunks.cubicchunks.core.server.PlayerCubeMap;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

public class CubeTrackerAccess implements ChunkTrackerAccess {
    public static Field cubeWatchersField;
    private static Method closestPlayerMethod;

    static {
        try {
            cubeWatchersField = PlayerCubeMap.class.getDeclaredField("cubeWatchers");
            cubeWatchersField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Terrarium.LOGGER.error("Failed to find cube watchers field", e);
        }
        try {
            closestPlayerMethod = CubeWatcher.class.getDeclaredMethod("getClosestPlayerDistance");
            closestPlayerMethod.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            Terrarium.LOGGER.error("Failed to find closets player distance method", e);
        }
    }

    private final WorldServer world;

    public CubeTrackerAccess(WorldServer world) {
        this.world = world;
    }

    @Override
    public LinkedHashSet<ChunkPos> getSortedQueuedColumns() {
        PlayerChunkMap chunkTracker = this.world.getPlayerChunkMap();
        if (chunkTracker instanceof PlayerCubeMap) {
            XYZMap<CubeWatcher> watchers = getWatchers((PlayerCubeMap) chunkTracker);

            Map<ChunkPos, ColumnState> columnStates = new HashMap<>();
            for (CubeWatcher watcher : watchers) {
                ChunkPos columnPos = new ChunkPos(watcher.getX(), watcher.getZ());
                ColumnState state = columnStates.get(columnPos);

                double distance = getClosestPlayerDistance(watcher);
                boolean queued = watcher.getCube() == null;
                if (queued) {
                    CubePos cubePos = new CubePos(watcher.getX(), watcher.getY(), watcher.getZ());
                    queued = !SavedCubeTracker.isSaved(this.world, cubePos);
                }

                if (state == null) {
                    columnStates.put(columnPos, new ColumnState(distance, queued));
                } else {
                    state.merge(distance, queued);
                }
            }

            LinkedHashSet<ChunkPos> queuedColumns = columnStates.entrySet().stream()
                    .filter(e -> e.getValue().queued)
                    .sorted(Comparator.comparingDouble(e -> e.getValue().distance))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            // cubic chunks requires surrounding chunks to be loaded for diffuse lighting
            for (ChunkPos column : columnStates.keySet()) {
                for (int z = -2; z <= 2; z++) {
                    for (int x = -2; x <= 2; x++) {
                        queuedColumns.add(new ChunkPos(column.x + x, column.z + z));
                    }
                }
            }

            return queuedColumns;
        }

        return new LinkedHashSet<>();
    }

    @SuppressWarnings("unchecked")
    private static XYZMap<CubeWatcher> getWatchers(PlayerCubeMap cubeTracker) {
        if (cubeWatchersField != null) {
            try {
                return (XYZMap<CubeWatcher>) cubeWatchersField.get(cubeTracker);
            } catch (Exception e) {
                Terrarium.LOGGER.error("Failed to get player cube entries", e);
            }
        }
        return new XYZMap<>(0.0F, 0);
    }

    private static double getClosestPlayerDistance(CubeWatcher watcher) {
        try {
            return (double) closestPlayerMethod.invoke(watcher);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Terrarium.LOGGER.error("Failed to get closest player distance", e);
        }
        return 0.0;
    }

    private static class ColumnState {
        private double distance;
        private boolean queued;

        ColumnState(double distance, boolean queued) {
            this.distance = distance;
            this.queued = queued;
        }

        void merge(double distance, boolean queued) {
            if (distance < this.distance) {
                this.distance = distance;
            }
            if (queued) {
                this.queued = true;
            }
        }
    }
}
