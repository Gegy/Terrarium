package net.gegy1000.terrarium.server.world.chunk;

import net.gegy1000.terrarium.Terrarium;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayerChunkMapHooks {
    private static Field chunkMapField;
    private static Field chunkMapEntriesField;
    private static Field entryChunkField;

    static {
        try {
            chunkMapField = ReflectionHelper.findField(WorldServer.class, "playerChunkMap", "field_73063_M");
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(chunkMapField, chunkMapField.getModifiers() & ~Modifier.FINAL);
        } catch (ReflectionHelper.UnableToFindFieldException | ReflectiveOperationException e) {
            Terrarium.LOGGER.error("Failed to find chunk map field", e);
        }
        try {
            chunkMapEntriesField = ReflectionHelper.findField(PlayerChunkMap.class, "entries", "field_111193_e");
        } catch (ReflectionHelper.UnableToFindFieldException e) {
            Terrarium.LOGGER.error("Failed to find chunk entries field", e);
        }
        try {
            entryChunkField = ReflectionHelper.findField(PlayerChunkMapEntry.class, "chunk", "field_187286_f");
        } catch (ReflectionHelper.UnableToFindFieldException e) {
            Terrarium.LOGGER.error("Failed to find entry chunk field", e);
        }
    }

    public static void hookWorldMap(WorldServer world) {
        if (!Loader.isModLoaded("cubicchunks")) {
            if (chunkMapField != null) {
                try {
                    chunkMapField.set(world, new Wrapper(world));
                } catch (Exception e) {
                    Terrarium.LOGGER.error("Failed to hook World chunk map", e);
                }
            }
        } else {
            // TODO
        }
    }

    @SuppressWarnings("unchecked")
    public static List<PlayerChunkMapEntry> getSortedChunkEntries(PlayerChunkMap chunkTracker) {
        List<PlayerChunkMapEntry> entries = PlayerChunkMapHooks.getEntries(chunkTracker);

        List<PlayerChunkMapEntry> sortedEntries = new ArrayList<>(entries);
        sortedEntries.sort(Comparator.comparingDouble(PlayerChunkMapEntry::getClosestPlayerDistance));

        return sortedEntries;
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

    private static void hookEntryChunk(PlayerChunkMapEntry entry, Chunk chunk) {
        if (entryChunkField != null) {
            try {
                entryChunkField.set(entry, chunk);
            } catch (Exception e) {
                Terrarium.LOGGER.error("Failed to hook PlayerChunkMapEntry chunk", e);
            }
        }
    }

    public static class Wrapper extends PlayerChunkMap {
        private final Map<ChunkPos, PlaceholderChunk> hookedChunks = new HashMap<>();

        public Wrapper(WorldServer world) {
            super(world);
        }

        public void hookChunk(ChunkPos chunk) {
            this.hookedChunks.put(chunk, new PlaceholderChunk(this.getWorldServer(), chunk.x, chunk.z));
        }

        public void unhookChunk(ChunkPos chunk) {
            this.hookedChunks.remove(chunk);
        }

        public Set<ChunkPos> getHookedChunks() {
            return this.hookedChunks.keySet();
        }

        @Override
        public void tick() {
            List<PlayerChunkMapEntry> entries = getEntries(this);

            List<PlayerChunkMapEntry> hookedEntries = new ArrayList<>(this.hookedChunks.size());
            for (PlayerChunkMapEntry entry : entries) {
                ChunkPos pos = entry.getPos();
                if (entry.getChunk() == null) {
                    if (this.hookedChunks.containsKey(pos)) {
                        hookEntryChunk(entry, this.hookedChunks.get(pos));
                        hookedEntries.add(entry);
                    }
                }
            }

            super.tick();

            for (PlayerChunkMapEntry hookedEntry : hookedEntries) {
                hookEntryChunk(hookedEntry, null);
            }
        }
    }
}
