package net.gegy1000.earth.server.world.data.op;

import net.gegy1000.earth.server.world.cover.CoverIds;
import net.gegy1000.earth.server.world.soil.SoilConfig;
import net.gegy1000.earth.server.world.soil.SoilConfigs;
import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ObjRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.UnsignedByteRaster;

public final class ProduceSoilOp {
    public static DataOp<ObjRaster<SoilConfig>> produce(DataOp<UnsignedByteRaster> coverId) {
        return coverId.map((coverRaster, engine, view) -> {
            ObjRaster<SoilConfig> result = ObjRaster.create(SoilConfigs.NORMAL_SOIL, view);
            coverRaster.iterate((cover, x, y) -> result.set(x, y, produceSoilConfig(cover)));
            return result;
        });
    }

    private static SoilConfig produceSoilConfig(int coverId) {
        if (coverId == CoverIds.PERMANENT_SNOW) {
            return SoilConfigs.PERMANENT_SNOW;
        } else if (coverId == CoverIds.WATER) {
            return SoilConfigs.UNDER_WATER;
        }

        return SoilConfigs.NORMAL_SOIL;
    }
}
