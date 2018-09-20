package net.gegy1000.terrarium.server.world.chunk.tracker;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.world.chunk.PlaceholderChunk;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ColumnChunkMapHooks implements ChunkTrackerHooks {
    public static Field chunkMapField;
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
            entryChunkField = ReflectionHelper.findField(PlayerChunkMapEntry.class, "chunk", "field_187286_f");
        } catch (ReflectionHelper.UnableToFindFieldException e) {
            Terrarium.LOGGER.error("Failed to find entry chunk field", e);
        }
    }

    private final WorldServer world;

    public ColumnChunkMapHooks(WorldServer world) {
        this.world = world;

        if (chunkMapField != null) {
            try {
                chunkMapField.set(world, new Wrapper(world));
            } catch (Exception e) {
                Terrarium.LOGGER.error("Failed to hook World chunk map", e);
            }
        }
    }

    private static void setEntryChunk(PlayerChunkMapEntry entry, Chunk chunk) {
        if (entryChunkField != null) {
            try {
                entryChunkField.set(entry, chunk);
            } catch (Exception e) {
                Terrarium.LOGGER.error("Failed to hook PlayerChunkMapEntry chunk", e);
            }
        }
    }

    @Override
    public void pauseChunk(ChunkPos pos) {
        Wrapper wrapper = this.get();
        if (wrapper != null) {
            wrapper.pauseChunk(pos);
        }
    }

    @Override
    public void unpauseChunk(ChunkPos pos) {
        Wrapper wrapper = this.get();
        if (wrapper != null) {
            wrapper.unpauseChunk(pos);
        }
    }

    @Override
    public Set<ChunkPos> getPausedChunks() {
        Wrapper wrapper = this.get();
        if (wrapper != null) {
            return wrapper.getPausedChunks();
        }
        return Collections.emptySet();
    }

    @Nullable
    private Wrapper get() {
        PlayerChunkMap chunkMap = this.world.getPlayerChunkMap();
        if (chunkMap instanceof Wrapper) {
            return (Wrapper) chunkMap;
        }
        return null;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == TerrariumCapabilities.chunkHooksCapability;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == TerrariumCapabilities.chunkHooksCapability) {
            return TerrariumCapabilities.chunkHooksCapability.cast(this);
        }
        return null;
    }

    private static class Wrapper extends PlayerChunkMap {
        private final Map<ChunkPos, PlaceholderChunk> pausedChunks = new HashMap<>();

        Wrapper(WorldServer world) {
            super(world);
        }

        void pauseChunk(ChunkPos chunk) {
            this.pausedChunks.put(chunk, new PlaceholderChunk(this.getWorldServer(), chunk.x, chunk.z));
        }

        void unpauseChunk(ChunkPos chunk) {
            this.pausedChunks.remove(chunk);
        }

        Set<ChunkPos> getPausedChunks() {
            return this.pausedChunks.keySet();
        }

        @Override
        public void tick() {
            List<PlayerChunkMapEntry> entries = ColumnTrackerAccess.getEntries(this);

            List<PlayerChunkMapEntry> pausedEntries = new ArrayList<>(this.pausedChunks.size());
            for (PlayerChunkMapEntry entry : entries) {
                ChunkPos pos = entry.getPos();
                if (entry.getChunk() == null) {
                    if (this.pausedChunks.containsKey(pos)) {
                        setEntryChunk(entry, this.pausedChunks.get(pos));
                        pausedEntries.add(entry);
                    }
                }
            }

            super.tick();

            for (PlayerChunkMapEntry pausedEntry : pausedEntries) {
                setEntryChunk(pausedEntry, null);
            }
        }
    }
}
