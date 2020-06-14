package net.gegy1000.earth.server.world.composer.structure.placement;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface StructurePlacement {
    boolean canSpawnAt(World world, int chunkX, int chunkZ);

    @Nullable
    BlockPos getClosestTo(World world, BlockPos pos, boolean findUnexplored);
}
