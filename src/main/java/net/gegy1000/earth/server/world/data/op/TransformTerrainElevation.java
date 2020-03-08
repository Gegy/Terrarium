package net.gegy1000.earth.server.world.data.op;

import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.minecraft.util.math.MathHelper;

public final class TransformTerrainElevation {
    private final double terrestrialScale;
    private final double oceanicScale;
    private final int offset;

    public TransformTerrainElevation(double terrestrialScale, double oceanicScale, int offset) {
        this.terrestrialScale = terrestrialScale;
        this.oceanicScale = oceanicScale;
        this.offset = offset;
    }

    public DataOp<ShortRaster> apply(DataOp<ShortRaster> source) {
        return source.map((raster, view) -> {
            raster.transform((elevation, x, y) -> {
                if (elevation >= 0) {
                    return (short) (MathHelper.ceil(elevation * this.terrestrialScale) + this.offset);
                } else {
                    return (short) (MathHelper.floor(elevation * this.oceanicScale) + this.offset);
                }
            });
            return raster;
        });
    }
}
