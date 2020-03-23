package net.gegy1000.earth.server.world.data.source;

import net.gegy1000.earth.server.util.ZoomLevels;
import net.gegy1000.earth.server.util.Zoomable;
import net.gegy1000.earth.server.world.data.source.reader.TerrariumRasterReader;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;

public final class ElevationSource {
    public static ZoomLevels zoomLevels() {
        return ZoomLevels.range(0, 6);
    }

    public static Zoomable<StdSource<ShortRaster>> source() {
        return StdSource.<ShortRaster>builder(zoomLevels())
                .cacheName("elevation")
                .read(input -> TerrariumRasterReader.read(input, ShortRaster.class))
                .endpoint(idx -> idx.elevation)
                .build();
    }
}
