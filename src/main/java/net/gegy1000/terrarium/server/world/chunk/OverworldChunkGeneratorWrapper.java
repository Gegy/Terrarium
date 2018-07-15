package net.gegy1000.terrarium.server.world.chunk;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorOverworld;

public class OverworldChunkGeneratorWrapper extends ChunkGeneratorOverworld {
    private final TerrariumChunkGenerator delegate;

    public OverworldChunkGeneratorWrapper(World world, TerrariumChunkGenerator delegate) {
        super(world, world.getWorldInfo().getSeed(), false, "");
        this.delegate = delegate;
    }

    @Override
    public Chunk generateChunk(int x, int z) {
        return this.delegate.generateChunk(x, z);
    }

    @Override
    public void populate(int x, int z) {
        this.delegate.populate(x, z);
    }

    @Override
    public void setBlocksInChunk(int chunkX, int chunkZ, ChunkPrimer primer) {
        this.delegate.populateTerrain(chunkX, chunkZ, primer);
    }
}
