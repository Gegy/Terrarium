package net.gegy1000.terrarium.server.world.chunk.tracker;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.CubeUnWatchEvent;
import io.github.opencubicchunks.cubicchunks.api.world.CubeWatchEvent;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import io.github.opencubicchunks.cubicchunks.core.server.CubeWatcher;
import io.github.opencubicchunks.cubicchunks.core.server.PlayerCubeMap;
import io.github.opencubicchunks.cubicchunks.core.server.chunkio.ICubeIO;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

public final class SavedCubeTracker {
    private static Field cubeWatcherPlayersField;

    static {
        try {
            cubeWatcherPlayersField = CubeWatcher.class.getDeclaredField("players");
            cubeWatcherPlayersField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Terrarium.LOGGER.error("Failed to find cube watcher players field", e);
        }
    }

    private static final EnumMap<DimensionType, WorldTracker> TRACKERS = new EnumMap<>(DimensionType.class);

    public static boolean isSaved(World world, CubePos cubePos) {
        WorldTracker tracker = TRACKERS.get(world.provider.getDimensionType());
        if (tracker != null) {
            return tracker.isSaved(cubePos);
        }
        return false;
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        TRACKERS.put(event.getWorld().provider.getDimensionType(), new WorldTracker());
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        TRACKERS.remove(event.getWorld().provider.getDimensionType());
    }

    @SubscribeEvent
    public static void onCubeWatch(CubeWatchEvent event) {
        ICube cube = event.getCube();
        if (cube == null) {
            return;
        }

        World world = cube.getWorld();
        WorldTracker tracker = TRACKERS.get(world.provider.getDimensionType());
        if (tracker != null) {
            CubePos coords = cube.getCoords();
            boolean saved = isCubeSaved(world, coords);
            tracker.mark(coords, saved);
        }
    }

    @SubscribeEvent
    public static void onCubeUnwatch(CubeUnWatchEvent event) {
        ICube cube = event.getCube();
        if (cube == null) {
            return;
        }

        World world = cube.getWorld();
        CubePos coords = cube.getCoords();

        if (!isCubeTracked(world, coords)) {
            WorldTracker tracker = TRACKERS.get(world.provider.getDimensionType());
            if (tracker != null) {
                tracker.remove(coords);
            }
        }
    }

    private static boolean isCubeTracked(World world, CubePos pos) {
        if (world instanceof WorldServer) {
            PlayerChunkMap chunkMap = ((WorldServer) world).getPlayerChunkMap();

            if (chunkMap instanceof PlayerCubeMap) {
                PlayerCubeMap cubeMap = (PlayerCubeMap) chunkMap;

                Collection<EntityPlayerMP> watchingPlayers = getWatchingPlayers(cubeMap, pos);
                return !watchingPlayers.isEmpty();
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private static Collection<EntityPlayerMP> getWatchingPlayers(PlayerCubeMap cubeMap, CubePos pos) {
        if (cubeWatcherPlayersField != null) {
            CubeWatcher cubeWatcher = cubeMap.getCubeWatcher(pos);
            if (cubeWatcher != null) {
                try {
                    return (List<EntityPlayerMP>) cubeWatcherPlayersField.get(cubeWatcher);
                } catch (IllegalAccessException e) {
                    Terrarium.LOGGER.warn("Failed to get cube watching players", e);
                }
            }
        }
        return Collections.emptyList();
    }

    private static boolean isCubeSaved(World world, CubePos pos) {
        if (world instanceof WorldServer) {
            ChunkProviderServer provider = ((WorldServer) world).getChunkProvider();
            if (provider instanceof CubeProviderServer) {
                ICubeIO io = ((CubeProviderServer) provider).getCubeIO();
                return io.cubeExists(pos.getX(), pos.getY(), pos.getZ());
            }
        }
        return false;
    }

    private static class WorldTracker {
        private final Object2BooleanMap<CubePos> savedStates = new Object2BooleanOpenHashMap<>();

        boolean isSaved(CubePos pos) {
            return this.savedStates.getBoolean(pos);
        }

        void mark(CubePos pos, boolean saved) {
            this.savedStates.put(pos, saved);
        }

        void remove(CubePos pos) {
            this.savedStates.removeBoolean(pos);
        }
    }
}
