package net.gegy1000.earth.server.world.data.op;

import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.cover.CoverIds;
import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.UnsignedByteRaster;

public final class ProduceCoverOp {
    public static DataOp<EnumRaster<Cover>> produce(DataOp<UnsignedByteRaster> coverId) {
        return coverId.map((coverIdRaster, engine, view) -> {
            EnumRaster<Cover> coverRaster = EnumRaster.create(Cover.NONE, view);
            coverIdRaster.iterate((id, x, y) -> coverRaster.set(x, y, CoverIds.get(id)));

            return coverRaster;
        });
    }
}
