package net.gegy1000.terrarium.server.world.chunk.tracker;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.util.XYZMap;
import io.github.opencubicchunks.cubicchunks.core.server.CubeWatcher;
import io.github.opencubicchunks.cubicchunks.core.server.PlayerCubeMap;
import io.github.opencubicchunks.cubicchunks.core.world.cube.Cube;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.world.chunk.PlaceholderCube;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CubeChunkMapHooks implements ChunkTrackerHooks {
    private final WorldServer world;

    private static Field cubeField;

    static {
        try {
            cubeField = ReflectionHelper.findField(CubeWatcher.class, "cube");
        } catch (ReflectionHelper.UnableToFindFieldException e) {
            Terrarium.LOGGER.error("Failed to find cube field", e);
        }
    }

    public CubeChunkMapHooks(WorldServer world) {
        this.world = world;

        if (ColumnChunkMapHooks.chunkMapField != null) {
            try {
                ColumnChunkMapHooks.chunkMapField.set(world, new Wrapper(world));
            } catch (Exception e) {
                Terrarium.LOGGER.error("Failed to hook World cube map", e);
            }
        }
    }

    @Override
    public void pauseChunk(ChunkPos pos) {
        Wrapper wrapper = this.get();
        if (wrapper != null) {
            wrapper.pauseColumn(pos);
        }
    }

    @Override
    public void unpauseChunk(ChunkPos pos) {
        Wrapper wrapper = this.get();
        if (wrapper != null) {
            wrapper.unpauseColumn(pos);
        }
    }

    @Override
    public Set<ChunkPos> getPausedChunks() {
        Wrapper wrapper = this.get();
        if (wrapper != null) {
            return wrapper.getPausedColumns();
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

    @SuppressWarnings("unchecked")
    private static XYZMap<CubeWatcher> getWatchers(PlayerCubeMap cubeTracker) {
        if (CubeTrackerAccess.cubeWatchersField != null) {
            try {
                return (XYZMap<CubeWatcher>) CubeTrackerAccess.cubeWatchersField.get(cubeTracker);
            } catch (Exception e) {
                Terrarium.LOGGER.error("Failed to get player column entries", e);
            }
        }
        return new XYZMap<>(0.0F, 0);
    }

    private static void setWatcherCube(CubeWatcher watcher, Cube cube) {
        if (cubeField != null) {
            try {
                cubeField.set(watcher, cube);
            } catch (Exception e) {
                Terrarium.LOGGER.error("Failed to hook watcher cube", e);
            }
        }
    }

    private static class Wrapper extends PlayerCubeMap {
        private final Set<ChunkPos> pausedColumns = new HashSet<>();
        private final Map<CubePos, PlaceholderCube> pausedCubes = new HashMap<>();

        Wrapper(WorldServer world) {
            super(world);
        }

        void pauseColumn(ChunkPos column) {
            this.pausedColumns.add(column);
        }

        void unpauseColumn(ChunkPos column) {
            this.pausedColumns.remove(column);

            Set<CubePos> unpausedCubes = this.pausedCubes.keySet().stream()
                    .filter(pos -> pos.getX() == column.x && pos.getZ() == column.z)
                    .collect(Collectors.toSet());
            for (CubePos cube : unpausedCubes) {
                this.pausedCubes.remove(cube);
            }
        }

        Set<ChunkPos> getPausedColumns() {
            return this.pausedColumns;
        }

        @Override
        public void tick() {
            XYZMap<CubeWatcher> entries = getWatchers(this);

            List<CubeWatcher> pausedEntries = new ArrayList<>(this.pausedCubes.size());
            for (CubeWatcher entry : entries) {
                ChunkPos columnPos = new ChunkPos(entry.getX(), entry.getZ());
                if (entry.getCube() == null && this.pausedColumns.contains(columnPos)) {
                    CubePos cubePos = new CubePos(entry);
                    setWatcherCube(entry, this.pausedCubes.computeIfAbsent(cubePos, this::createPlaceholder));
                    pausedEntries.add(entry);
                }
            }

            super.tick();

            for (CubeWatcher pausedEntry : pausedEntries) {
                setWatcherCube(pausedEntry, null);
            }
        }

        private PlaceholderCube createPlaceholder(CubePos pos) {
            return new PlaceholderCube(this.getWorldServer(), pos);
        }
    }
}
