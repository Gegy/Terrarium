package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import net.gegy1000.terrarium.server.world.chunk.PseudoRandomMap;
import net.gegy1000.terrarium.server.world.region.GenerationRegionHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class BedrockSurfaceComposer implements SurfaceComposer {
    private static final long BEDROCK_SCATTER_SEED = 5654549466233716589L;

    private final PseudoRandomMap scatterMap;

    private final IBlockState block;
    private final int scatterRange;

    public BedrockSurfaceComposer(World world, IBlockState block, int scatterRange) {
        this.scatterMap = new PseudoRandomMap(world, BEDROCK_SCATTER_SEED);

        this.block = block;
        this.scatterRange = scatterRange;
    }

    @Override
    public void composeSurface(ChunkPrimer primer, GenerationRegionHandler regionHandler, int chunkX, int chunkZ) {
        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                this.scatterMap.initPosSeed(globalX + localX, globalZ + localZ);
                for (int localY = 0; localY < this.scatterRange; localY++) {
                    if (localY == 0 || localY <= this.scatterMap.nextInt(this.scatterRange)) {
                        primer.setBlockState(localX, localY, localZ, this.block);
                    }
                }
            }
        }
    }
}
