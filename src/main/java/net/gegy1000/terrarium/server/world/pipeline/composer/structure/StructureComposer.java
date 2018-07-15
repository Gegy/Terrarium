package net.gegy1000.terrarium.server.world.pipeline.composer.structure;

import net.gegy1000.terrarium.server.world.pipeline.composer.ChunkComposer;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nullable;

public interface StructureComposer extends ChunkComposer {
    void composeStructures(IChunkGenerator generator, ChunkPrimer primer, RegionGenerationHandler regionHandler, int chunkX, int chunkZ);

    void populateStructures(World world, RegionGenerationHandler regionHandler, int chunkX, int chunkZ);

    boolean isInsideStructure(World world, String structureName, BlockPos pos);

    @Nullable
    BlockPos getNearestStructure(World world, String structureName, BlockPos pos, boolean findUnexplored);
}
