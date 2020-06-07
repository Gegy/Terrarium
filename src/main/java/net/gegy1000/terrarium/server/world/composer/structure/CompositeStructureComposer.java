package net.gegy1000.terrarium.server.world.composer.structure;

import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.gengen.api.writer.ChunkPrimeWriter;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collection;

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
        double nearestDistance = Integer.MAX_VALUE;

        for (StructureComposer composer : this.composers) {
            BlockPos structure = composer.getClosestStructure(terrarium, world, name, pos, findUnexplored);
            if (structure != null) {
                double distance = structure.distanceSq(pos);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = structure;
                }
            }
        }

        return nearest;
    }
}
