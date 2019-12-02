package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPrimeWriter;
import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public class BedrockSurfaceComposer implements SurfaceComposer {
    private static final long BEDROCK_SCATTER_SEED = 5654549466233716589L;

    private final SpatialRandom scatterMap;

    private final IBlockState block;
    private final int scatterRange;

    public BedrockSurfaceComposer(World world, IBlockState block, int scatterRange) {
        this.scatterMap = new SpatialRandom(world, BEDROCK_SCATTER_SEED);

        this.block = block;
        this.scatterRange = scatterRange;
    }

    @Override
    public void composeSurface(ColumnData data, CubicPos pos, ChunkPrimeWriter writer) {
        int minY = pos.getMinY();
        int maxY = pos.getMaxY();
        if (minY >= this.scatterRange || maxY < 0) {
            return;
        }

        int globalX = pos.getMinX();
        int globalZ = pos.getMinZ();

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                this.scatterMap.setSeed(globalX + localX, globalZ + localZ);
                for (int localY = minY; localY < this.scatterRange; localY++) {
                    if (localY == 0 || localY <= this.scatterMap.nextInt(this.scatterRange)) {
                        writer.set(localX, localY, localZ, this.block);
                    }
                }
            }
        }
    }
}
