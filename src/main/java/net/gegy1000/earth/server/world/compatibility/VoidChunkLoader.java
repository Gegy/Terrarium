package net.gegy1000.earth.server.world.compatibility;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.IChunkLoader;

import javax.annotation.Nullable;

public final class VoidChunkLoader implements IChunkLoader {
    public static final VoidChunkLoader INSTANCE = new VoidChunkLoader();

    private VoidChunkLoader() {
    }

    @Nullable
    @Override
    public Chunk loadChunk(World world, int x, int z) {
        return null;
    }

    @Override
    public void saveChunk(World world, Chunk chunk) {
    }

    @Override
    public void saveExtraChunkData(World world, Chunk chunk) {
    }

    @Override
    public void chunkTick() {
    }

    @Override
    public void flush() {
    }

    @Override
    public boolean isChunkGeneratedAt(int x, int z) {
        return false;
    }
}
