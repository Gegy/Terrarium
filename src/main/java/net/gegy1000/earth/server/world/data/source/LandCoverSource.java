package net.gegy1000.earth.server.world.data.source;

import net.gegy1000.earth.server.util.ZoomLevels;
import net.gegy1000.earth.server.util.Zoomable;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.data.source.reader.TerrariumRasterReader;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;

public final class LandCoverSource {
    public static ZoomLevels zoomLevels() {
        return ZoomLevels.range(0, 4);
    }

    public static Zoomable<StdSource<EnumRaster<Cover>>> source() {
        return StdSource.<EnumRaster<Cover>>builder(zoomLevels())
                .cacheName("landcover")
                .endpoint("landcover")
                .read(input -> {
                    UByteRaster raw = TerrariumRasterReader.read(input, UByteRaster.class);

                    int width = raw.getWidth();
                    int height = raw.getHeight();

                    EnumRaster<Cover> raster = EnumRaster.create(Cover.NO, width, height);
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            raster.set(x, y, Cover.byId(raw.get(x, y)));
                        }
                    }

                    return raster;
                })
                .build();
    }
}
