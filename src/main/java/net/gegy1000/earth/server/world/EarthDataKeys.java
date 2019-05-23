package net.gegy1000.earth.server.world;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.earth.server.world.soil.SoilConfig;
import net.gegy1000.terrarium.server.world.pipeline.data.Data;
import net.gegy1000.terrarium.server.world.pipeline.data.DataKey;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ObjRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.UnsignedByteRaster;
import net.minecraft.util.ResourceLocation;

public class EarthDataKeys {
    public static final DataKey<ShortRaster> HEIGHT = create("height");
    public static final DataKey<UnsignedByteRaster> SLOPE = create("slope");
    public static final DataKey<EnumRaster<Cover>> COVER = create("cover");
    public static final DataKey<EnumRaster<Landform>> LANDFORM = create("landform");
    public static final DataKey<ShortRaster> WATER_LEVEL = create("water");
    public static final DataKey<ObjRaster<SoilConfig>> SOIL = create("soil");
    public static final DataKey<FloatRaster> AVERAGE_TEMPERATURE = create("temperature");
    public static final DataKey<ShortRaster> ANNUAL_RAINFALL = create("rainfall");

    private static <T extends Data> DataKey<T> create(String name) {
        return new DataKey<>(new ResourceLocation(TerrariumEarth.MODID, name));
    }
}
