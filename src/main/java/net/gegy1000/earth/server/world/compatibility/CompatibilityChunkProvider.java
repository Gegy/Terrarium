package net.gegy1000.earth.server.world.compatibility;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

class CompatibilityChunkProvider extends ChunkProviderServer {
    private final ColumnCompatibilityWorld compatibilityWorld;

    public CompatibilityChunkProvider(ColumnCompatibilityWorld world) {
        super(world, VoidChunkLoader.INSTANCE, new VoidGenerator(world));
        this.compatibilityWorld = world;
    }

    void clear() {
        this.loadedChunks.clear();
    }

    @Override
    public void queueUnload(Chunk chunk) {
    }

    @Override
    public void queueUnloadAll() {
    }

    @Nullable
    @Override
    public Chunk loadChunk(int x, int z, @Nullable Runnable runnable) {
        Chunk chunk = this.getLoadedChunk(x, z);

        if (chunk == null) {
            Chunk parentChunk = this.compatibilityWorld.parent.getChunk(x, z);
            chunk = new CompatibilityChunk(this.compatibilityWorld, parentChunk);

            this.loadedChunks.put(ChunkPos.asLong(x, z), chunk);
        }

        if (runnable != null) {
            runnable.run();
        }

        return chunk;
    }

    @Override
    public Chunk provideChunk(int x, int z) {
        return this.loadChunk(x, z, null);
    }

    @Override
    public boolean tick() {
        return false;
    }

    @Override
    public boolean isChunkGeneratedAt(int x, int z) {
        return this.compatibilityWorld.parent.isChunkGeneratedAt(x, z);
    }

    @Override
    public boolean saveChunks(boolean all) {
        return true;
    }

    @Override
    public boolean canSave() {
        return false;
    }

    @Override
    public String makeString() {
        return "ColumnCompatibilityChunkProvider";
    }

    private static class VoidGenerator implements IChunkGenerator {
        private final World world;

        private VoidGenerator(World world) {
            this.world = world;
        }

        @Override
        public Chunk generateChunk(int x, int z) {
            return new Chunk(this.world, x, z);
        }

        @Override
        public void populate(int x, int z) {
        }

        @Override
        public boolean generateStructures(Chunk chunk, int x, int z) {
            return false;
        }

        @Override
        public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType type, BlockPos pos) {
            return Collections.emptyList();
        }

        @Nullable
        @Override
        public BlockPos getNearestStructurePos(World world, String name, BlockPos origin, boolean findUnexplored) {
            return null;
        }

        @Override
        public void recreateStructures(Chunk chunk, int x, int z) {
        }

        @Override
        public boolean isInsideStructure(World world, String name, BlockPos pos) {
            return false;
        }
    }
}
