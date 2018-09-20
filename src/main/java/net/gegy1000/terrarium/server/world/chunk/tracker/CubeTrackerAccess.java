package net.gegy1000.terrarium.server.world.chunk.tracker;

import com.google.common.collect.Lists;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
            List<CubeWatcher> watchers = getSortedWatchers((PlayerCubeMap) chunkTracker);

            Set<ChunkPos> queuedColumns = this.collectQueuedColumns(watchers);

            Set<TrackedColumn> columns = new LinkedHashSet<>();
            for (CubeWatcher watcher : watchers) {
                ChunkPos pos = new ChunkPos(watcher.getX(), watcher.getZ());
                columns.add(new TrackedColumn(pos, queuedColumns.contains(pos)));
            }

            return columns;
        }

        return Collections.emptyList();
    }

    private Set<ChunkPos> collectQueuedColumns(List<CubeWatcher> watchers) {
        Set<ChunkPos> queuedColumns = new HashSet<>();
        for (CubeWatcher watcher : watchers) {
            if (watcher.getCube() == null || watcher.getCube() instanceof HookedChunkMarker) {
                queuedColumns.add(new ChunkPos(watcher.getX(), watcher.getZ()));
            }
        }
        return queuedColumns;
    }

    @SuppressWarnings("unchecked")
    private static List<CubeWatcher> getSortedWatchers(PlayerCubeMap cubeTracker) {
        if (cubeWatchersField != null) {
            try {
                XYZMap<CubeWatcher> watchers = (XYZMap<CubeWatcher>) cubeWatchersField.get(cubeTracker);

                // TODO: Sort once in column form
                List<CubeWatcher> sortedWatchers = Lists.newArrayList(watchers);
                sortedWatchers.sort(Comparator.comparingDouble(CubeTrackerAccess::getClosestPlayerDistance));

                return sortedWatchers;
            } catch (Exception e) {
                Terrarium.LOGGER.error("Failed to get player column entries", e);
            }
        }
        return Collections.emptyList();
    }

    private static double getClosestPlayerDistance(CubeWatcher watcher) {
        try {
            return (double) closestPlayerMethod.invoke(watcher);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Terrarium.LOGGER.error("Failed to get closest player distance", e);
        }
        return 0.0;
    }
}
