package net.gegy1000.earth.server.world.data.op;

import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.cover.CoverId;
import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.EnumRaster;

public final class ProduceCoverOp {
    public static DataOp<EnumRaster<Cover>> produce(DataOp<EnumRaster<CoverId>> coverId) {
        return coverId.map((coverIdRaster, engine, view) -> {
            EnumRaster<Cover> coverRaster = EnumRaster.create(Cover.NONE, view);
            coverIdRaster.iterate((id, x, y) -> coverRaster.set(x, y, id.getCover()));

            return coverRaster;
        });
    }
}
