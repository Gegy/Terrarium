package net.gegy1000.earth.server.world.data.op;

import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
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

    public DataOp<ShortRaster> apply(DataOp<FloatRaster> source) {
        return source.map((raster, view) -> {
            ShortRaster result = ShortRaster.create(view);
            raster.iterate((elevation, x, y) -> {
                short value;
                if (elevation >= 0) {
                    value = (short) (MathHelper.ceil(elevation * this.terrestrialScale) + this.offset);
                } else {
                    value = (short) (MathHelper.floor(elevation * this.oceanicScale) + this.offset);
                }
                result.set(x, y, value);
            });
            return result;
        });
    }
}
