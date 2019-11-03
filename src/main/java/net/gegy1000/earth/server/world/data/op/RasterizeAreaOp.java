package net.gegy1000.earth.server.world.data.op;

import net.gegy1000.earth.server.world.data.AreaData;
import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.BitRaster;
import net.gegy1000.terrarium.server.world.rasterization.RasterCanvas;

public final class RasterizeAreaOp {
    public static DataOp<BitRaster> apply(DataOp<AreaData> area) {
        return area.map((areaData, engine, view) -> {
            BitRaster raster = BitRaster.create(view);

            RasterCanvas canvas = RasterCanvas.of(view);
            canvas.setColor(1);
            canvas.fill(areaData.getArea());

            for (int localY = 0; localY < view.getHeight(); localY++) {
                for (int localX = 0; localX < view.getWidth(); localX++) {
                    if (canvas.getData(localX, localY) == 1) {
                        raster.put(localX, localY);
                    }
                }
            }

            return raster;
        });
    }
}
