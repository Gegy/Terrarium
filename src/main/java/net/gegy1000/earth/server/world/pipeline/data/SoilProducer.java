package net.gegy1000.earth.server.world.pipeline.data;

import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.soil.SoilConfig;
import net.gegy1000.earth.server.world.soil.SoilConfigs;
import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ObjRaster;

import java.util.concurrent.CompletableFuture;

public final class SoilProducer {
    public static DataOp<ObjRaster<SoilConfig>> produce(DataOp<ObjRaster<Cover>> coverClassification) {
        return DataOp.of((engine, view) -> {
            CompletableFuture<ObjRaster<Cover>> coverFuture = engine.load(coverClassification, view);
            return coverFuture.thenApply(coverRaster -> {
                ObjRaster<SoilConfig> result = ObjRaster.create(SoilConfigs.NORMAL_SOIL, view);
                coverRaster.iterate((cover, x, y) -> result.set(x, y, produceSoilConfig(cover)));
                return result;
            });
        });
    }

    private static SoilConfig produceSoilConfig(Cover cover) {
        if (cover == Cover.PERMANENT_SNOW) {
            return SoilConfigs.PERMANENT_SNOW;
        } else if (cover == Cover.WATER) {
            return SoilConfigs.UNDER_WATER;
        }

        return SoilConfigs.NORMAL_SOIL;
    }
}
