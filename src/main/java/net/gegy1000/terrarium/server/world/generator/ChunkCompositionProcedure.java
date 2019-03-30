package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nullable;
import java.util.Set;

public interface ChunkCompositionProcedure {
    void composeSurface(RegionGenerationHandler regionHandler, CubicPos pos, ChunkPrimeWriter writer);

    void composeDecoration(RegionGenerationHandler regionHandler, CubicPos pos, ChunkPopulationWriter writer);

    Biome[] composeBiomes(RegionGenerationHandler regionHandler, int chunkX, int chunkZ);

    void composeStructures(IChunkGenerator generator, ChunkPrimer primer, RegionGenerationHandler regionHandler, int chunkX, int chunkZ);

    void populateStructures(World world, RegionGenerationHandler regionHandler, int chunkX, int chunkZ);

    boolean isInsideStructure(World world, String structureName, BlockPos pos);

    @Nullable
    BlockPos getNearestStructure(World world, String structureName, BlockPos pos, boolean findUnexplored);

    Set<RegionComponentType<?>> getSurfaceDependencies();

    Set<RegionComponentType<?>> getStructureDependencies();

    Set<RegionComponentType<?>> getDecorationDependencies();

    Set<RegionComponentType<?>> getBiomeDependencies();
}
