package net.gegy1000.earth.server.world.data.op;

import net.gegy1000.earth.server.world.cover.CoverIds;
import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;

public final class ProduceLandformsOp {
    public static DataOp<EnumRaster<Landform>> produce(DataOp<ShortRaster> height, DataOp<UByteRaster> coverId) {
        return DataOp.join2(height, coverId).map((tup, view) -> {
            ShortRaster heightRaster = tup.a;
            UByteRaster coverIdRaster = tup.b;

            EnumRaster<Landform> landformRaster = EnumRaster.create(Landform.LAND, view);
            coverIdRaster.iterate((id, x, y) -> {
                if (heightRaster.get(x, y) <= 0) {
                    landformRaster.set(x, y, Landform.SEA);
                } else if (id == CoverIds.WATER) {
                    landformRaster.set(x, y, Landform.LAKE_OR_RIVER);
                } else {
                    landformRaster.set(x, y, Landform.LAND);
                }
            });

            return landformRaster;
        });
    }
}
