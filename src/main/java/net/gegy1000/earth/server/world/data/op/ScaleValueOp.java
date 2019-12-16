package net.gegy1000.earth.server.world.data.op;

import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.minecraft.util.math.MathHelper;

public final class ScaleValueOp {
    private final double scale;

    public ScaleValueOp(double scale) {
        this.scale = scale;
    }

    public DataOp<FloatRaster> applyFloat(DataOp<FloatRaster> source) {
        return source.map((raster, view) -> {
            raster.transform((sourceValue, x, y) -> (float) (sourceValue * this.scale));
            return raster;
        });
    }

    public DataOp<ShortRaster> applyShort(DataOp<ShortRaster> source) {
        return source.map((raster, view) -> {
            raster.transform((sourceValue, x, y) -> (short) MathHelper.ceil(sourceValue * this.scale));
            return raster;
        });
    }
}
