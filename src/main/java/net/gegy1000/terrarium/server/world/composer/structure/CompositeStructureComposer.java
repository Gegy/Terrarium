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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class CompositeStructureComposer implements StructureComposer {
    private final StructureComposer[] composers;

    private CompositeStructureComposer(StructureComposer[] composers) {
        this.composers = composers;
    }

    public static CompositeStructureComposer of(StructureComposer... composers) {
        return new CompositeStructureComposer(composers);
    }

    public static CompositeStructureComposer of(Collection<StructureComposer> composers) {
        return new CompositeStructureComposer(composers.toArray(new StructureComposer[0]));
    }

    @Override
    public void prepareStructures(TerrariumWorld terrarium, CubicPos pos) {
        for (StructureComposer composer : this.composers) {
            composer.prepareStructures(terrarium, pos);
        }
    }

    @Override
    public void primeStructures(TerrariumWorld terrarium, CubicPos pos, ChunkPrimeWriter writer) {
        for (StructureComposer composer : this.composers) {
            composer.primeStructures(terrarium, pos, writer);
        }
    }

    @Override
    public void populateStructures(TerrariumWorld terrarium, CubicPos pos, ChunkPopulationWriter writer) {
        for (StructureComposer composer : this.composers) {
            composer.populateStructures(terrarium, pos, writer);
        }
    }

    @Override
    public boolean isInsideStructure(TerrariumWorld terrarium, World world, String name, BlockPos pos) {
        for (StructureComposer composer : this.composers) {
            if (composer.isInsideStructure(terrarium, world, name, pos)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public BlockPos getClosestStructure(TerrariumWorld terrarium, World world, String name, BlockPos pos, boolean findUnexplored) {
        BlockPos nearest = null;
        double nearestDistanceSq = Double.MAX_VALUE;

        for (StructureComposer composer : this.composers) {
            BlockPos structure = composer.getClosestStructure(terrarium, world, name, pos, findUnexplored);
            if (structure != null) {
                double distanceSq = structure.distanceSq(pos);
                if (distanceSq < nearestDistanceSq) {
                    nearestDistanceSq = distanceSq;
                    nearest = structure;
                }
            }
        }

        return nearest;
    }

    @Nullable
    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(TerrariumWorld terrarium, World world, EnumCreatureType type, BlockPos pos) {
        List<Biome.SpawnListEntry> result = null;

        for (StructureComposer composer : this.composers) {
            List<Biome.SpawnListEntry> creatures = composer.getPossibleCreatures(terrarium, world, type, pos);
            if (creatures == null) continue;

            if (result == null) {
                result = new ArrayList<>(creatures.size());
            }

            result.addAll(creatures);
        }

        return result;
    }
}
