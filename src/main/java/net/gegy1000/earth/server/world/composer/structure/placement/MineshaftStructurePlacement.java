package net.gegy1000.earth.server.world.composer.structure.placement;

import net.gegy1000.terrarium.server.util.SpiralIterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.vecmath.Point2i;
import java.util.Random;

public final class MineshaftStructurePlacement implements StructurePlacement {
    private static final int SEARCH_RADIUS = 1000;

    private final double chance;

    private final Random random = new Random(0);

    public MineshaftStructurePlacement(double chance) {
        this.chance = chance;
    }

    private Random getRandomFor(World world, int chunkX, int chunkZ) {
        this.random.setSeed((long) (chunkX ^ chunkZ) ^ world.getSeed());
        this.random.nextInt();

        return this.random;
    }

    @Override
    @Nullable
    public BlockPos getClosestTo(World world, BlockPos origin, boolean findUnexplored) {
        int originChunkX = origin.getX() >> 4;
        int originChunkZ = origin.getZ() >> 4;

        for (Point2i point : SpiralIterator.of(SEARCH_RADIUS)) {
            int chunkX = originChunkX + point.x;
            int chunkZ = originChunkZ + point.y;

            if (this.canSpawnAt(world, chunkX, chunkZ) && (!findUnexplored || !world.isChunkGeneratedAt(chunkX, chunkZ))) {
                return new BlockPos((chunkX << 4) + 8, 64, (chunkZ << 4) + 8);
            }
        }

        return null;
    }

    @Override
    public boolean canSpawnAt(World world, int chunkX, int chunkZ) {
        Random random = this.getRandomFor(world, chunkX, chunkZ);
        return random.nextDouble() < this.chance && random.nextInt(80) < Math.max(Math.abs(chunkX), Math.abs(chunkZ));
    }
}
