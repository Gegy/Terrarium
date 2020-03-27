package net.gegy1000.earth.server.world;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.ecology.soil.SoilClass;
import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public final class EarthDataKeys {
    public static final DataKey<ShortRaster> TERRAIN_HEIGHT = create("terrain_height", ShortRaster::create);
    public static final DataKey<UByteRaster> SLOPE = create("slope", UByteRaster::create);
    public static final DataKey<EnumRaster<Cover>> COVER = create("cover", view -> EnumRaster.create(Cover.NO, view));
    public static final DataKey<EnumRaster<Landform>> LANDFORM = create("landform", view -> EnumRaster.create(Landform.LAND, view));
    public static final DataKey<ShortRaster> WATER_LEVEL = create("water", ShortRaster::create);
    public static final DataKey<FloatRaster> ELEVATION_METERS = create("elevation_meters", FloatRaster::create);
    public static final DataKey<FloatRaster> MIN_TEMPERATURE = create("min_temperature", view -> FloatRaster.create(view, 10.0F));
    public static final DataKey<FloatRaster> MEAN_TEMPERATURE = create("mean_temperature", view -> FloatRaster.create(view, 14.0F));
    public static final DataKey<ShortRaster> ANNUAL_RAINFALL = create("annual_rainfall", view -> ShortRaster.create(view, 300));
    public static final DataKey<UByteRaster> CATION_EXCHANGE_CAPACITY = create("cation_exchange_capacity", view -> UByteRaster.create(view, 10));
    public static final DataKey<ShortRaster> ORGANIC_CARBON_CONTENT = create("organic_carbon_content", view -> ShortRaster.create(view, 10));
    public static final DataKey<UByteRaster> SOIL_PH = create("soil_ph", view -> UByteRaster.create(view, 70));
    public static final DataKey<UByteRaster> CLAY_CONTENT = create("clay_content", view -> UByteRaster.create(view, 33));
    public static final DataKey<UByteRaster> SILT_CONTENT = create("silt_content", view -> UByteRaster.create(view, 33));
    public static final DataKey<UByteRaster> SAND_CONTENT = create("sand_content", view -> UByteRaster.create(view, 33));
    public static final DataKey<EnumRaster<SoilClass>> SOIL_CLASS = create("soil_class", view -> EnumRaster.create(SoilClass.NO, view));

    private static <T> DataKey<T> create(String name, Function<DataView, T> createDefault) {
        return new DataKey<>(new ResourceLocation(TerrariumEarth.ID, name), createDefault);
    }
}
