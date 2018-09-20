package net.gegy1000.terrarium.server.world.chunk.tracker;

import io.github.opencubicchunks.cubicchunks.api.util.XYZMap;
import io.github.opencubicchunks.cubicchunks.core.server.CubeWatcher;
import io.github.opencubicchunks.cubicchunks.core.server.PlayerCubeMap;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CubeTrackerAccess implements ChunkTrackerAccess {
    public static Field cubeWatchersField;
    private static Method closestPlayerMethod;

    static {
        try {
            cubeWatchersField = ReflectionHelper.findField(PlayerCubeMap.class, "cubeWatchers");
        } catch (ReflectionHelper.UnableToFindFieldException e) {
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
    public Collection<TrackedColumn> getSortedTrackedColumns() {
        PlayerChunkMap chunkTracker = this.world.getPlayerChunkMap();

        if (chunkTracker instanceof PlayerCubeMap) {
            XYZMap<CubeWatcher> watchers = getWatchers((PlayerCubeMap) chunkTracker);

            Map<ChunkPos, ColumnState> columnStates = new HashMap<>();
            for (CubeWatcher watcher : watchers) {
                ChunkPos columnPos = new ChunkPos(watcher.getX(), watcher.getZ());
                ColumnState state = columnStates.get(columnPos);

                double distance = getClosestPlayerDistance(watcher);
                boolean queued = watcher.getCube() == null || watcher.getCube() instanceof HookedChunkMarker;

                if (state == null) {
                    columnStates.put(columnPos, new ColumnState(distance, queued));
                } else {
                    state.merge(distance, queued);
                }
            }

            List<TrackedColumn> columns = new ArrayList<>(columnStates.size());
            for (Map.Entry<ChunkPos, ColumnState> entry : columnStates.entrySet()) {
                columns.add(new TrackedColumn(entry.getKey(), entry.getValue().queued));
            }

            columns.sort(Comparator.comparingDouble(c -> columnStates.get(c.getPos()).distance));

            return columns;
        }

        return Collections.emptyList();
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

        public ColumnState(double distance, boolean queued) {
            this.distance = distance;
            this.queued = queued;
        }

        public void merge(double distance, boolean queued) {
            if (distance < this.distance) {
                this.distance = distance;
            }
            if (queued) {
                this.queued = true;
            }
        }
    }
}
