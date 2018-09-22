package net.gegy1000.terrarium.server.world.chunk.tracker;

import net.gegy1000.cubicglue.CubicGlue;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.Set;

public interface ChunkTrackerHooks extends ICapabilityProvider {
    @Nullable
    static ChunkTrackerHooks createHooks(WorldServer world) {
        if (CubicGlue.isCubic(world)) {
            return createCubicHooks(world);
        } else {
            return new ColumnChunkMapHooks(world);
        }
    }

    static ChunkTrackerHooks createCubicHooks(WorldServer world) {
        return new CubeChunkMapHooks(world);
    }

    void pauseChunk(ChunkPos pos);

    void unpauseChunk(ChunkPos pos);

    Set<ChunkPos> getPausedChunks();
}
