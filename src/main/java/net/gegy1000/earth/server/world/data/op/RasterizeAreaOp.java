package net.gegy1000.earth.server.world.data.op;

import net.gegy1000.terrarium.server.util.Profiler;
import net.gegy1000.terrarium.server.util.ThreadedProfiler;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.raster.BitRaster;
import net.gegy1000.terrarium.server.world.rasterization.RasterCanvas;

import java.awt.geom.Area;

public final class RasterizeAreaOp {
    public static DataOp<BitRaster> apply(DataOp<Area> areaOp) {
        return areaOp.map((area, view) -> {
            Profiler profiler = ThreadedProfiler.get();
            try (Profiler.Handle rasterizeArea = profiler.push("rasterize_area")) {
                BitRaster raster = BitRaster.create(view);

                RasterCanvas canvas = new RasterCanvas(view.width(), view.height());
                canvas.setColor(1);
                canvas.fill(area);

                for (int y = 0; y < view.height(); y++) {
                    for (int x = 0; x < view.width(); x++) {
                        if (canvas.getData(x, y) == 1) {
                            raster.put(x, y);
                        }
                    }
                }

                return raster;
            }
        });
    }
}
