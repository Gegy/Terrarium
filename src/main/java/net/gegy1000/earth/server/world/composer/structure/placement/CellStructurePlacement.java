package net.gegy1000.earth.server.world.composer.structure.placement;

import net.gegy1000.terrarium.server.util.SpiralIterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.vecmath.Point2i;
import java.util.Random;

public final class CellStructurePlacement implements StructurePlacement {
    private static final int SEARCH_RADIUS = 100;

    private final int cellSize;
    private final int cellBorder;
    private final int seed;

    private Predicate predicate = (world, chunkX, chunkZ) -> true;

    private final Point2i mutablePoint = new Point2i();

    public CellStructurePlacement(int cellSize, int cellBorder, int seed) {
        this.cellSize = cellSize;
        this.cellBorder = cellBorder;
        this.seed = seed;
    }

    public CellStructurePlacement setPredicate(Predicate predicate) {
        this.predicate = predicate;
        return this;
    }

    protected boolean canSpawnAt(World world, Point2i columnPos) {
        return this.predicate.canSpawnAt(world, columnPos.x, columnPos.y);
    }

    @Override
    @Nullable
    public BlockPos getClosestTo(World world, BlockPos origin, boolean findUnexplored) {
        int originChunkX = origin.getX() >> 4;
        int originChunkZ = origin.getZ() >> 4;

        for (Point2i point : SpiralIterator.of(SEARCH_RADIUS)) {
            int chunkX = originChunkX + this.cellBorder * point.x;
            int chunkZ = originChunkZ + this.cellBorder * point.y;

            Point2i cell = this.getCellFor(chunkX, chunkZ);
            Point2i spawnChunk = this.getSpawnChunkForCell(world, cell.x, cell.y);

            if (this.canSpawnAt(world, spawnChunk)) {
                if (!findUnexplored || !world.isChunkGeneratedAt(spawnChunk.x, spawnChunk.y)) {
                    return new BlockPos((spawnChunk.x << 4) + 8, world.getSeaLevel(), (spawnChunk.y << 4) + 8);
                }
            }
        }

        return null;
    }

    @Override
    public boolean canSpawnAt(World world, int chunkX, int chunkZ) {
        Point2i cell = this.getCellFor(chunkX, chunkZ);
        Point2i spawnChunk = this.getSpawnChunkForCell(world, cell.x, cell.y);

        if (chunkX == spawnChunk.x && chunkZ == spawnChunk.y) {
            return this.canSpawnAt(world, spawnChunk);
        }

        return false;
    }

    private Point2i getCellFor(int chunkX, int chunkZ) {
        this.mutablePoint.x = Math.floorDiv(chunkX, this.cellSize);
        this.mutablePoint.y = Math.floorDiv(chunkZ, this.cellSize);
        return this.mutablePoint;
    }

    private Point2i getSpawnChunkForCell(World world, int cellX, int cellZ) {
        Random random = world.setRandomSeed(cellX, cellZ, this.seed);

        int minCellX = cellX * this.cellSize;
        int minCellZ = cellZ * this.cellSize;

        this.mutablePoint.x = minCellX + random.nextInt(this.cellSize - this.cellBorder);
        this.mutablePoint.y = minCellZ + random.nextInt(this.cellSize - this.cellBorder);

        return this.mutablePoint;
    }

    public interface Predicate {
        boolean canSpawnAt(World world, int chunkX, int chunkZ);
    }
}
