package net.gegy1000.earth.server.world.cover;

import net.gegy1000.terrarium.server.world.cover.CoverSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.UnsignedByteRasterTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.layer.GenLayer;

public abstract class EarthSurfaceGenerator extends CoverSurfaceGenerator<EarthCoverContext> {
    protected EarthSurfaceGenerator(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
        super(context, coverType);
    }

    @Override
    protected void coverFromLayer(IBlockState[] blockBuffer, int originX, int originZ, GenLayer layer, BlockProvider blockProvider) {
        UnsignedByteRasterTile slopeRaster = this.context.getSlopeRaster();
        super.coverFromLayer(blockBuffer, originX, originZ, layer, (sampledValue, localX, localZ) -> {
            IBlockState state = blockProvider.provideBlock(sampledValue, localX, localZ);
            if (slopeRaster.getByte(localX, localZ) >= CLIFF_SLOPE) {
                if (state == GRASS || state == PODZOL) {
                    state = COARSE_DIRT;
                } else if (state == COARSE_DIRT) {
                    state = COBBLESTONE;
                } else if (state == SAND) {
                    state = SANDSTONE;
                } else if (state == GRAVEL) {
                    state = COBBLESTONE;
                }
            }
            return state;
        });
    }
}
