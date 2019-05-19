package net.gegy1000.terrarium.server.world.chunk.tracker;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.EnumMap;

public final class SavedColumnTracker {
    private static final EnumMap<DimensionType, WorldTracker> TRACKERS = new EnumMap<>(DimensionType.class);

    public static boolean isSaved(World world, ChunkPos columnPos) {
        WorldTracker tracker = TRACKERS.get(world.provider.getDimensionType());
        if (tracker != null) {
            return tracker.isSaved(columnPos);
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
    public static void onColumnWatch(ChunkWatchEvent.Watch event) {
        Chunk chunk = event.getChunkInstance();
        if (chunk == null) {
            return;
        }

        World world = chunk.getWorld();
        WorldTracker tracker = TRACKERS.get(world.provider.getDimensionType());
        if (tracker != null) {
            boolean saved = isColumnSaved(world, chunk.x, chunk.z);
            tracker.mark(chunk.getPos(), saved);
        }
    }

    @SubscribeEvent
    public static void onColumnUnwatch(ChunkWatchEvent.UnWatch event) {
        Chunk chunk = event.getChunkInstance();
        if (chunk == null) {
            return;
        }

        World world = chunk.getWorld();

        if (!isColumnTracked(world, chunk.x, chunk.z)) {
            WorldTracker tracker = TRACKERS.get(world.provider.getDimensionType());
            if (tracker != null) {
                tracker.remove(chunk.getPos());
            }
        }
    }

    private static boolean isColumnTracked(World world, int x, int z) {
        if (world instanceof WorldServer) {
            PlayerChunkMap chunkMap = ((WorldServer) world).getPlayerChunkMap();
            return chunkMap.contains(x, z);
        }
        return false;
    }

    private static boolean isColumnSaved(World world, int x, int z) {
        if (world instanceof WorldServer) {
            return ((WorldServer) world).getChunkProvider().chunkLoader.isChunkGeneratedAt(x, z);
        }
        return false;
    }

    private static class WorldTracker {
        private final Object2BooleanMap<ChunkPos> savedStates = new Object2BooleanOpenHashMap<>();

        boolean isSaved(ChunkPos pos) {
            return this.savedStates.getBoolean(pos);
        }

        void mark(ChunkPos pos, boolean saved) {
            this.savedStates.put(pos, saved);
        }

        void remove(ChunkPos pos) {
            this.savedStates.removeBoolean(pos);
        }
    }
}
