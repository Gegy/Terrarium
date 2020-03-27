package net.gegy1000.earth.server.world.data.source;

import net.gegy1000.earth.server.util.ZoomLevels;
import net.gegy1000.earth.server.util.Zoomable;
import net.gegy1000.earth.server.world.data.source.reader.TerrariumRasterReader;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;

public final class ElevationSource {
    public static ZoomLevels zoomLevels() {
        return ZoomLevels.range(0, 6);
    }

    public static Zoomable<StdSource<FloatRaster>> source() {
        return StdSource.<FloatRaster>builder(zoomLevels())
                .cacheName("elevation")
                .read(input -> {
                    ShortRaster shortRaster = TerrariumRasterReader.read(input, ShortRaster.class);

                    FloatRaster result = FloatRaster.create(shortRaster.getWidth(), shortRaster.getHeight());
                    shortRaster.iterate((value, x, y) -> result.set(x, y, value));

                    return result;
                })
                .endpoint(idx -> idx.elevation)
                .build();
    }
}
