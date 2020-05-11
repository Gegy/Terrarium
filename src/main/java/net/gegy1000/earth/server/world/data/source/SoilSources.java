package net.gegy1000.earth.server.world.data.source;

import net.gegy1000.earth.server.util.zoom.ZoomLevels;
import net.gegy1000.earth.server.util.zoom.Zoomable;
import net.gegy1000.earth.server.world.data.source.reader.TerrariumRasterReader;
import net.gegy1000.earth.server.world.ecology.soil.SoilSuborder;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;

public final class SoilSources {
    public static ZoomLevels zoomLevels() {
        return ZoomLevels.range(0, 4);
    }

    private static Zoomable<StdSource<ShortRaster>> soilShort(String name) {
        return StdSource.<ShortRaster>builder(zoomLevels())
                .cacheName("soil/" + name)
                .endpoint(name)
                .read(input -> TerrariumRasterReader.read(input, ShortRaster.class))
                .build();
    }

    private static Zoomable<StdSource<UByteRaster>> soilUByte(String name) {
        return StdSource.<UByteRaster>builder(zoomLevels())
                .cacheName("soil/" + name)
                .endpoint(name)
                .read(input -> UByteRaster.copyFrom(TerrariumRasterReader.read(input, ShortRaster.class)))
                .build();
    }

    public static Zoomable<StdSource<ShortRaster>> cationExchangeCapacity() {
        return soilShort("cec");
    }

    public static Zoomable<StdSource<ShortRaster>> organicCarbonContent() {
        return soilShort("occ");
    }

    public static Zoomable<StdSource<UByteRaster>> ph() {
        return soilUByte("ph");
    }

    public static Zoomable<StdSource<UByteRaster>> clayContent() {
        return soilUByte("clay");
    }

    public static Zoomable<StdSource<UByteRaster>> siltContent() {
        return soilUByte("silt");
    }

    public static Zoomable<StdSource<UByteRaster>> sandContent() {
        return soilUByte("sand");
    }

    public static Zoomable<StdSource<EnumRaster<SoilSuborder>>> soilClass() {
        return StdSource.<EnumRaster<SoilSuborder>>builder(zoomLevels())
                .cacheName("soil/usda")
                .endpoint("usda")
                .read(input -> {
                    UByteRaster raw = TerrariumRasterReader.read(input, UByteRaster.class);

                    int width = raw.getWidth();
                    int height = raw.getHeight();

                    EnumRaster<SoilSuborder> raster = EnumRaster.create(SoilSuborder.NO, width, height);
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            raster.set(x, y, SoilSuborder.byId(raw.get(x, y)));
                        }
                    }

                    return raster;
                })
                .build();
    }
}
