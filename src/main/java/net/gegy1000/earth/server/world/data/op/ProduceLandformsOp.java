package net.gegy1000.earth.server.world.data.op;

import net.gegy1000.earth.server.world.cover.CoverId;
import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;

import java.util.concurrent.CompletableFuture;

public final class ProduceLandformsOp {
    public static DataOp<EnumRaster<Landform>> produce(DataOp<ShortRaster> height, DataOp<EnumRaster<CoverId>> coverId) {
        return DataOp.of((engine, view) -> {
            CompletableFuture<ShortRaster> heightFuture = engine.load(height, view);
            CompletableFuture<EnumRaster<CoverId>> coverIdFuture = engine.load(coverId, view);

            return CompletableFuture.allOf(heightFuture, coverIdFuture)
                    .thenApply(v -> {
                        ShortRaster heightRaster = heightFuture.join();
                        EnumRaster<CoverId> coverIdRaster = coverIdFuture.join();

                        EnumRaster<Landform> landformRaster = EnumRaster.create(Landform.LAND, view);
                        coverIdRaster.iterate((id, x, y) -> {
                            if (id == CoverId.WATER) {
                                if (heightRaster.get(x, y) <= 1) {
                                    landformRaster.set(x, y, Landform.SEA);
                                } else {
                                    landformRaster.set(x, y, Landform.LAKE_OR_RIVER);
                                }
                            } else {
                                landformRaster.set(x, y, Landform.LAND);
                            }
                        });

                        return landformRaster;
                    });
        });
    }
}
