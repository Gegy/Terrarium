package net.gegy1000.earth.server.world.data.source;

import net.gegy1000.earth.server.util.ZoomLevels;
import net.gegy1000.earth.server.util.Zoomable;
import net.gegy1000.earth.server.world.data.source.reader.TerrariumRasterReader;
import net.gegy1000.earth.server.world.ecology.soil.SoilClass;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;

public final class SoilSources {
    public static ZoomLevels zoomLevels() {
        return ZoomLevels.range(0, 4);
    }

    private static Zoomable<StdSource<ShortRaster>> genericSoil(String name) {
        return StdSource.<ShortRaster>builder(zoomLevels())
                .cacheName("soil/" + name)
                .endpoint(name)
                .read(input -> TerrariumRasterReader.read(input, ShortRaster.class))
                .build();
    }

    public static Zoomable<StdSource<ShortRaster>> cationExchangeCapacity() {
        return genericSoil("cec");
    }

    public static Zoomable<StdSource<ShortRaster>> organicCarbonContent() {
        return genericSoil("occ");
    }

    public static Zoomable<StdSource<ShortRaster>> ph() {
        return genericSoil("ph");
    }

    public static Zoomable<StdSource<ShortRaster>> clayContent() {
        return genericSoil("clay");
    }

    public static Zoomable<StdSource<ShortRaster>> siltContent() {
        return genericSoil("silt");
    }

    public static Zoomable<StdSource<ShortRaster>> sandContent() {
        return genericSoil("sand");
    }

    public static Zoomable<StdSource<EnumRaster<SoilClass>>> soilClass() {
        return StdSource.<EnumRaster<SoilClass>>builder(zoomLevels())
                .cacheName("soil/usda")
                .endpoint("usda")
                .read(input -> {
                    UByteRaster raw = TerrariumRasterReader.read(input, UByteRaster.class);

                    int width = raw.getWidth();
                    int height = raw.getHeight();

                    EnumRaster<SoilClass> raster = EnumRaster.create(SoilClass.NO, width, height);
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            raster.set(x, y, SoilClass.byId(raw.get(x, y)));
                        }
                    }

                    return raster;
                })
                .build();
    }
}
