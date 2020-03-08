package net.gegy1000.earth.server.world;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;
import net.minecraft.util.ResourceLocation;

public final class EarthDataKeys {
    public static final DataKey<ShortRaster> TERRAIN_HEIGHT = create("terrain_height");
    public static final DataKey<UByteRaster> SLOPE = create("slope");
    public static final DataKey<EnumRaster<Cover>> COVER = create("cover");
    public static final DataKey<EnumRaster<Landform>> LANDFORM = create("landform");
    public static final DataKey<ShortRaster> WATER_LEVEL = create("water");
    public static final DataKey<ShortRaster> ELEVATION_METERS = create("elevation_meters");
    public static final DataKey<FloatRaster> MIN_TEMPERATURE = create("min_temperature");
    public static final DataKey<FloatRaster> MEAN_TEMPERATURE = create("mean_temperature");
    public static final DataKey<ShortRaster> ANNUAL_RAINFALL = create("annual_rainfall");
    public static final DataKey<UByteRaster> CATION_EXCHANGE_CAPACITY = create("cation_exchange_capacity");
    public static final DataKey<ShortRaster> ORGANIC_CARBON_CONTENT = create("organic_carbon_content");
    public static final DataKey<UByteRaster> SOIL_PH = create("soil_ph");
    public static final DataKey<UByteRaster> CLAY_CONTENT = create("clay_content");
    public static final DataKey<UByteRaster> SILT_CONTENT = create("silt_content");
    public static final DataKey<UByteRaster> SAND_CONTENT = create("sand_content");

    private static <T> DataKey<T> create(String name) {
        return new DataKey<>(new ResourceLocation(TerrariumEarth.ID, name));
    }
}
