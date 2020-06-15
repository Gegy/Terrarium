package net.gegy1000.earth.server.world.data.op;

import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;

public final class AddOp {
    public static DataOp<FloatRaster> applyFloats(DataOp<FloatRaster> leftOp, DataOp<FloatRaster> rightOp) {
        return DataOp.map2(leftOp, rightOp, (view, left, right) -> {
            left.transform((value, x, y) -> value + right.get(x, y));
            return left;
        });
    }
}
