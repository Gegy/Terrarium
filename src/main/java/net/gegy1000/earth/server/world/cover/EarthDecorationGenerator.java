package net.gegy1000.earth.server.world.cover;

import net.gegy1000.terrarium.server.world.cover.CoverDecorationGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

public abstract class EarthDecorationGenerator extends CoverDecorationGenerator<EarthCoverContext> {
    protected EarthDecorationGenerator(EarthCoverContext context, CoverType coverType) {
        super(context, coverType);
    }

    @Override
    protected boolean tryPlace(Random random, BlockPos pos, int localX, int localZ) {
        boolean place = super.tryPlace(random, pos, localX, localZ);
        if (place) {
            int slope = this.context.getSlopeRaster().getUnsigned(localX, localZ);
            return slope < MOUNTAINOUS_SLOPE || random.nextInt(2) == 0;
        }
        return false;
    }
}
