package net.gegy1000.terrarium.server.world.pipeline.composer.structure;

import net.gegy1000.terrarium.server.world.pipeline.data.ColumnDataCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

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
    public void composeStructures(IChunkGenerator generator, ChunkPrimer primer, ColumnDataCache dataCache, int chunkX, int chunkZ) {
        for (StructureComposer composer : this.composers) {
            composer.composeStructures(generator, primer, dataCache, chunkX, chunkZ);
        }
    }

    @Override
    public void populateStructures(World world, ColumnDataCache dataCache, int chunkX, int chunkZ) {
        for (StructureComposer composer : this.composers) {
            composer.populateStructures(world, dataCache, chunkX, chunkZ);
        }
    }

    @Override
    public boolean isInsideStructure(World world, String structureName, BlockPos pos) {
        for (StructureComposer composer : this.composers) {
            if (composer.isInsideStructure(world, structureName, pos)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public BlockPos getNearestStructure(World world, String structureName, BlockPos pos, boolean findUnexplored) {
        BlockPos nearest = null;
        double nearestDistance = Integer.MAX_VALUE;

        for (StructureComposer composer : this.composers) {
            BlockPos structure = composer.getNearestStructure(world, structureName, pos, findUnexplored);
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
