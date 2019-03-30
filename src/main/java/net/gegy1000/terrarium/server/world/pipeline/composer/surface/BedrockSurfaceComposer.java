package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.cubicglue.util.PseudoRandomMap;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

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
    public void composeSurface(RegionGenerationHandler regionHandler, CubicPos pos, ChunkPrimeWriter writer) {
        int minY = pos.getMinY();
        int maxY = pos.getMaxY();
        if (minY >= this.scatterRange || maxY < 0) {
            return;
        }

        int globalX = pos.getMinX();
        int globalZ = pos.getMinZ();

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                this.scatterMap.initPosSeed(globalX + localX, globalZ + localZ);
                for (int localY = minY; localY < this.scatterRange; localY++) {
                    if (localY == 0 || localY <= this.scatterMap.nextInt(this.scatterRange)) {
                        writer.set(localX, localY, localZ, this.block);
                    }
                }
            }
        }
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[0];
    }
}
