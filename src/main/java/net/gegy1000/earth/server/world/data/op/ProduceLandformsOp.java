package net.gegy1000.earth.server.world.data.op;

import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;

public final class ProduceLandformsOp {
    public static DataOp<EnumRaster<Landform>> produce(DataOp<ShortRaster> height, DataOp<EnumRaster<Cover>> cover) {
        return DataOp.join2(height, cover).map((tup, view) -> {
            ShortRaster heightRaster = tup.a;
            EnumRaster<Cover> coverIdRaster = tup.b;

            EnumRaster<Landform> landformRaster = EnumRaster.create(Landform.LAND, view);
            coverIdRaster.iterate((id, x, y) -> {
                if (heightRaster.get(x, y) <= 0) {
                    landformRaster.set(x, y, Landform.SEA);
                } else if (id == Cover.WATER) {
                    landformRaster.set(x, y, Landform.LAKE_OR_RIVER);
                } else {
                    landformRaster.set(x, y, Landform.LAND);
                }
            });

            return landformRaster;
        });
    }
}
