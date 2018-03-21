package net.gegy1000.earth.server.world.cover;

import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.cover.CoverSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.layer.GenLayer;

public abstract class EarthSurfaceGenerator extends CoverSurfaceGenerator {
    protected EarthSurfaceGenerator(CoverGenerationContext context, CoverType coverType) {
        super(context, coverType);
    }

    @Override
    protected void coverFromLayer(IBlockState[] blockBuffer, int originX, int originZ, GenLayer layer, BlockProvider blockProvider) {
        super.coverFromLayer(blockBuffer, originX, originZ, layer, (sampledValue, slope) -> {
            IBlockState state = blockProvider.provideBlock(sampledValue, slope);
            if (slope >= CLIFF_SLOPE) {
                if (state == GRASS || state == PODZOL) {
                    state = COARSE_DIRT;
                } else if (state == COARSE_DIRT) {
                    state = COBBLESTONE;
                } else if (state == SAND) {
                    state = SANDSTONE;
                }
            }
            return state;
        });
    }
}
