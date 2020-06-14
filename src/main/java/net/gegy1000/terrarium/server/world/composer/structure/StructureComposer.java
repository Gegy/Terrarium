package net.gegy1000.terrarium.server.world.composer.structure;

import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.gengen.api.writer.ChunkPrimeWriter;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.util.List;

public interface StructureComposer {
    void prepareStructures(TerrariumWorld terrarium, CubicPos pos);

    void primeStructures(TerrariumWorld terrarium, CubicPos pos, ChunkPrimeWriter writer);

    void populateStructures(TerrariumWorld terrarium, CubicPos pos, ChunkPopulationWriter writer);

    boolean isInsideStructure(TerrariumWorld terrarium, World world, String name, BlockPos pos);

    @Nullable
    BlockPos getClosestStructure(TerrariumWorld terrarium, World world, String name, BlockPos pos, boolean findUnexplored);

    @Nullable
    List<Biome.SpawnListEntry> getPossibleCreatures(TerrariumWorld terrarium, World world, EnumCreatureType type, BlockPos pos);
}
