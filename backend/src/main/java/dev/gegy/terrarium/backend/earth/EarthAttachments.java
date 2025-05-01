package dev.gegy.terrarium.backend.earth;

import dev.gegy.terrarium.backend.GeoAttachment;
import dev.gegy.terrarium.backend.GeoAttachmentSet;
import dev.gegy.terrarium.backend.GeoChunk;
import dev.gegy.terrarium.backend.earth.climate.RainfallRaster;
import dev.gegy.terrarium.backend.earth.climate.TemperatureRaster;
import dev.gegy.terrarium.backend.earth.cover.Cover;
import dev.gegy.terrarium.backend.earth.soil.SoilSuborder;
import dev.gegy.terrarium.backend.raster.EnumRaster;
import dev.gegy.terrarium.backend.raster.ShortRaster;
import dev.gegy.terrarium.backend.raster.UnsignedByteRaster;

import java.util.Optional;

public record EarthAttachments(
        ShortRaster elevation,
        EnumRaster<Cover> landCover,
        UnsignedByteRaster cationExchangeCapacity,
        ShortRaster organicCarbonContent,
        UnsignedByteRaster soilPh,
        UnsignedByteRaster clayContent,
        UnsignedByteRaster siltContent,
        UnsignedByteRaster sandContent,
        EnumRaster<SoilSuborder> soilSuborder,
        TemperatureRaster meanTemperature,
        TemperatureRaster minTemperature,
        RainfallRaster annualRainfall
) {
    public static final GeoAttachment<ShortRaster> ELEVATION = GeoAttachment.register("elevation", ShortRaster.TYPE);
    public static final GeoAttachment<EnumRaster<Cover>> LAND_COVER = GeoAttachment.register("land_cover", EnumRaster.type(Cover.NONE, Cover.CODEC));
    public static final GeoAttachment<UnsignedByteRaster> CATION_EXCHANGE_CAPACITY = GeoAttachment.register("cation_exchange_capacity", UnsignedByteRaster.TYPE);
    public static final GeoAttachment<ShortRaster> ORGANIC_CARBON_CONTENT = GeoAttachment.register("organic_carbon_content", ShortRaster.TYPE);
    public static final GeoAttachment<UnsignedByteRaster> SOIL_PH = GeoAttachment.register("soil_ph", UnsignedByteRaster.TYPE);
    public static final GeoAttachment<UnsignedByteRaster> CLAY_CONTENT = GeoAttachment.register("clay_content", UnsignedByteRaster.TYPE);
    public static final GeoAttachment<UnsignedByteRaster> SILT_CONTENT = GeoAttachment.register("silt_content", UnsignedByteRaster.TYPE);
    public static final GeoAttachment<UnsignedByteRaster> SAND_CONTENT = GeoAttachment.register("sand_content", UnsignedByteRaster.TYPE);
    public static final GeoAttachment<EnumRaster<SoilSuborder>> SOIL_SUBORDER = GeoAttachment.register("soil_suborder", EnumRaster.type(SoilSuborder.NONE, SoilSuborder.CODEC));
    public static final GeoAttachment<TemperatureRaster> MEAN_TEMPERATURE = GeoAttachment.register("mean_temperature", TemperatureRaster.TYPE);
    public static final GeoAttachment<TemperatureRaster> MIN_TEMPERATURE = GeoAttachment.register("min_temperature", TemperatureRaster.TYPE);
    public static final GeoAttachment<RainfallRaster> ANNUAL_RAINFALL = GeoAttachment.register("annual_rainfall", RainfallRaster.TYPE);

    public static final GeoAttachmentSet REQUIRED_SET = GeoAttachmentSet.of(
            ELEVATION,
            LAND_COVER,
            CATION_EXCHANGE_CAPACITY,
            ORGANIC_CARBON_CONTENT,
            SOIL_PH,
            CLAY_CONTENT,
            SILT_CONTENT,
            SAND_CONTENT,
            SOIL_SUBORDER,
            MEAN_TEMPERATURE,
            MIN_TEMPERATURE,
            ANNUAL_RAINFALL
    );

    public static Optional<EarthAttachments> from(final GeoChunk chunk) {
        return chunk.requireAll(REQUIRED_SET).map(completeChunk -> new EarthAttachments(
                completeChunk.getOrThrow(ELEVATION),
                completeChunk.getOrThrow(LAND_COVER),
                completeChunk.getOrThrow(CATION_EXCHANGE_CAPACITY),
                completeChunk.getOrThrow(ORGANIC_CARBON_CONTENT),
                completeChunk.getOrThrow(SOIL_PH),
                completeChunk.getOrThrow(CLAY_CONTENT),
                completeChunk.getOrThrow(SILT_CONTENT),
                completeChunk.getOrThrow(SAND_CONTENT),
                completeChunk.getOrThrow(SOIL_SUBORDER),
                completeChunk.getOrThrow(MEAN_TEMPERATURE),
                completeChunk.getOrThrow(MIN_TEMPERATURE),
                completeChunk.getOrThrow(ANNUAL_RAINFALL)
        ));
    }
}
