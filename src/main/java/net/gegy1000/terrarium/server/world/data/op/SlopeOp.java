package net.gegy1000.terrarium.server.world.data.op;

import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;
import net.minecraft.util.math.MathHelper;

public final class SlopeOp {
    private static final int CACHE_SIZE = 128;
    private static final int CACHE_RES = 4;
    private static final byte[] SLOPE_TABLE = new byte[CACHE_SIZE];

    static {
        for (int idx = 0; idx < CACHE_SIZE; idx++) {
            float ratio = (float) idx / CACHE_RES;
            double slope = Math.toDegrees(Math.atan(ratio));
            SLOPE_TABLE[idx] = (byte) Math.floor(slope);
        }
    }

    public static DataOp<UByteRaster> from(DataOp<FloatRaster> heights, float heightScale) {
        return DataOp.of((view, ctx) -> {
            DataView sourceView = view.grow(1);
            return heights.apply(sourceView, ctx)
                    .map(opt -> opt.map(source -> {
                        UByteRaster result = UByteRaster.create(view);
                        for (int localY = 0; localY < view.height(); localY++) {
                            for (int localX = 0; localX < view.width(); localX++) {
                                int slope = computeSlope(source, localX + 1, localY + 1, heightScale);
                                result.set(localX, localY, slope);
                            }
                        }
                        return result;
                    }));
        });
    }

    private static int computeSlope(FloatRaster source, int x, int y, float heightScale) {
        float current = source.get(x, y);

        double slope = Math.max(
                Math.max(
                        slope((current - source.get(x - 1, y - 1)) * heightScale),
                        slope((current - source.get(x + 1, y - 1)) * heightScale)
                ),
                Math.max(
                        slope((current - source.get(x - 1, y + 1)) * heightScale),
                        slope((current - source.get(x + 1, y + 1)) * heightScale)
                )
        );

        return MathHelper.floor(slope);
    }

    private static byte slope(float rise) {
        int idx = (int) (Math.abs(rise) * CACHE_RES);
        if (idx >= CACHE_SIZE) return 90;
        return SLOPE_TABLE[idx];
    }
}
