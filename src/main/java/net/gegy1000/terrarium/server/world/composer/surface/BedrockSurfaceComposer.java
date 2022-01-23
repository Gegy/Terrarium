package net.gegy1000.terrarium.server.world.composer.surface;

import dev.gegy.gengen.api.CubicPos;
import dev.gegy.gengen.api.writer.ChunkPrimeWriter;
import dev.gegy.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.data.DataSample;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BedrockSurfaceComposer implements SurfaceComposer {
    private static final long SEED = 5654549466233716589L;
    private static final IBlockState BLOCK = Blocks.BEDROCK.getDefaultState();

    private final DataKey<?> heightRaster;

    private final SpatialRandom random;
    private final int scatterRange;

    public BedrockSurfaceComposer(World world, DataKey<?> heightRaster, int scatterRange) {
        this.random = new SpatialRandom(world, SEED);
        this.heightRaster = heightRaster;

        this.scatterRange = scatterRange;
    }

    @Override
    public void composeSurface(TerrariumWorld terrarium, DataSample data, CubicPos pos, ChunkPrimeWriter writer) {
        int minY = pos.getMinY();
        int maxY = pos.getMaxY();
        if (minY >= this.scatterRange || maxY < 0) {
            return;
        }

        if (!data.contains(this.heightRaster)) {
            return;
        }

        int globalX = pos.getMinX();
        int globalZ = pos.getMinZ();

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                this.random.setSeed(globalX + localX, globalZ + localZ);
                for (int localY = minY; localY < this.scatterRange; localY++) {
                    if (localY == 0 || localY <= this.random.nextInt(this.scatterRange)) {
                        writer.set(localX, localY, localZ, BLOCK);
                    }
                }
            }
        }
    }
}
