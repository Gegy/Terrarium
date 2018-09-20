package net.gegy1000.terrarium.server.world.cover;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public abstract class CoverDecorationGenerator<T extends CoverGenerationContext> extends CoverGenerator<T> {
    private final Set<BlockPos> intersectionPoints = new HashSet<>();
    private int intersectionRange;

    protected CoverDecorationGenerator(T context, CoverType<T> coverType) {
        super(context, coverType);
    }

    public abstract void decorate(CubicPos chunkPos, ChunkPopulationWriter writer, Random random);

    protected void decorateScatter(Random random, CubicPos chunkPos, ChunkPopulationWriter writer, int count, ScatterDecorateConsumer decorator) {
        World world = this.context.getWorld();
        CoverRasterTile coverRaster = this.context.getCoverRaster();

        BlockPos.MutableBlockPos pos = this.mutablePos;

        for (int i = 0; i < count; i++) {
            int scatterX = random.nextInt(16);
            int scatterZ = random.nextInt(16);

            if (coverRaster.get(scatterX, scatterZ) == this.coverType) {
                pos.setPos(chunkPos.getCenterX() + scatterX, 0, chunkPos.getCenterZ() + scatterZ);

                if (this.tryPlace(random, pos, scatterX, scatterZ)) {
                    BlockPos topBlock = writer.getSurface(pos);
                    if (topBlock == null) {
                        continue;
                    }

                    if (!world.isAirBlock(topBlock)) {
                        world.setBlockToAir(topBlock);
                    }

                    decorator.handlePoint(topBlock, scatterX, scatterZ);
                }
            }
        }
    }

    protected boolean tryPlace(Random random, BlockPos pos, int localX, int localZ) {
        if (this.intersectionRange > 0) {
            if (this.checkHorizontalIntersection(pos)) {
                return false;
            }
            this.intersectionPoints.add(pos.toImmutable());
        }
        return true;
    }

    private boolean checkHorizontalIntersection(BlockPos pos) {
        int range = this.intersectionRange;
        for (BlockPos intersectionPoint : this.intersectionPoints) {
            int deltaX = Math.abs(intersectionPoint.getX() - pos.getX());
            int deltaZ = Math.abs(intersectionPoint.getZ() - pos.getZ());
            if (deltaX <= range && deltaZ <= range) {
                return true;
            }
        }
        return false;
    }

    protected final void preventIntersection(int range) {
        this.intersectionRange = range;
    }

    protected final void stopIntersectionPrevention() {
        this.intersectionRange = -1;
        this.intersectionPoints.clear();
    }

    protected interface ScatterDecorateConsumer {
        void handlePoint(BlockPos pos, int localX, int localZ);
    }

    public static class Empty<T extends CoverGenerationContext> extends CoverDecorationGenerator<T> {
        public Empty(T context, CoverType<T> coverType) {
            super(context, coverType);
        }

        @Override
        public void decorate(CubicPos chunkPos, ChunkPopulationWriter writer, Random random) {
        }
    }
}
