package net.gegy1000.terrarium.server.world.composer.structure;

import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.gengen.api.writer.ChunkPrimeWriter;
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
    public void prepareStructures(CubicPos pos) {
        for (StructureComposer composer : this.composers) {
            composer.prepareStructures(pos);
        }
    }

    @Override
    public void primeStructures(CubicPos pos, ChunkPrimeWriter writer) {
        for (StructureComposer composer : this.composers) {
            composer.primeStructures(pos, writer);
        }
    }

    @Override
    public void populateStructures(CubicPos pos, ChunkPopulationWriter writer) {
        for (StructureComposer composer : this.composers) {
            composer.populateStructures(pos, writer);
        }
    }

    @Override
    public boolean isInsideStructure(World world, String name, BlockPos pos) {
        for (StructureComposer composer : this.composers) {
            if (composer.isInsideStructure(world, name, pos)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public BlockPos getClosestStructure(World world, String name, BlockPos pos, boolean findUnexplored) {
        BlockPos nearest = null;
        double nearestDistance = Integer.MAX_VALUE;

        for (StructureComposer composer : this.composers) {
            BlockPos structure = composer.getClosestStructure(world, name, pos, findUnexplored);
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
