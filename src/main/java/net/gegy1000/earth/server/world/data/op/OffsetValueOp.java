package net.gegy1000.earth.server.world.data.op;

import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;

public final class OffsetValueOp {
    private final double offset;

    public OffsetValueOp(double offset) {
        this.offset = offset;
    }

    public DataOp<ShortRaster> apply(DataOp<ShortRaster> heights) {
        return heights.map((heightRaster, view) -> {
            heightRaster.transform((source, x, y) -> (short) Math.round(source + this.offset));
            return heightRaster;
        });
    }
}
