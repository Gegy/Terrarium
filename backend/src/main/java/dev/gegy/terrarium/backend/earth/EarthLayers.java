package dev.gegy.terrarium.backend.earth;

import dev.gegy.terrarium.backend.GeoChunk;
import dev.gegy.terrarium.backend.GeoView;
import dev.gegy.terrarium.backend.earth.climate.ClimateRasterSamplers;
import dev.gegy.terrarium.backend.earth.climate.RainfallRaster;
import dev.gegy.terrarium.backend.earth.climate.TemperatureRaster;
import dev.gegy.terrarium.backend.earth.cover.Cover;
import dev.gegy.terrarium.backend.earth.soil.SoilSuborder;
import dev.gegy.terrarium.backend.layer.GeoLayer;
import dev.gegy.terrarium.backend.projection.Projection;
import dev.gegy.terrarium.backend.raster.EnumRaster;
import dev.gegy.terrarium.backend.raster.RasterShape;
import dev.gegy.terrarium.backend.raster.ShortRaster;
import dev.gegy.terrarium.backend.raster.UnsignedByteRaster;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public record EarthLayers(
        GeoLayer<ShortRaster> elevation,
        GeoLayer<EnumRaster<Cover>> landCover,
        GeoLayer<UnsignedByteRaster> cationExchangeCapacity,
        GeoLayer<ShortRaster> organicCarbonContent,
        GeoLayer<UnsignedByteRaster> soilPh,
        GeoLayer<UnsignedByteRaster> clayContent,
        GeoLayer<UnsignedByteRaster> siltContent,
        GeoLayer<UnsignedByteRaster> sandContent,
        GeoLayer<EnumRaster<SoilSuborder>> soilSuborder,
        GeoLayer<TemperatureRaster> meanTemperature,
        GeoLayer<TemperatureRaster> minTemperature,
        GeoLayer<RainfallRaster> annualRainfall,
        Executor executor
) implements GeoLayer<GeoChunk> {
    public static EarthLayers create(final EarthTiles tiles, final Projection projection, final Executor executor) {
        final ClimateRasterSamplers climate = tiles.climateSamplers();
        return new EarthLayers(
                projection.createInterpolatedLayer(tiles.elevation(), executor),
                projection.createVoronoiLayer(tiles.landCover(), executor),
                projection.createInterpolatedLayer(tiles.cationExchangeCapacity(), executor),
                projection.createInterpolatedLayer(tiles.organicCarbonContent(), executor),
                projection.createInterpolatedLayer(tiles.soilPh(), executor),
                projection.createInterpolatedLayer(tiles.clayContent(), executor),
                projection.createInterpolatedLayer(tiles.siltContent(), executor),
                projection.createInterpolatedLayer(tiles.sandContent(), executor),
                projection.createVoronoiLayer(tiles.soilSuborder(), executor),
                projection.createInterpolatedLayer(climate.meanTemperature(), executor),
                projection.createInterpolatedLayer(climate.minTemperature(), executor),
                projection.createInterpolatedLayer(climate.annualRainfall(), executor),
                executor
        );
    }

    @Override
    public CompletableFuture<Optional<GeoChunk>> get(final GeoView sourceView, final RasterShape outputShape) {
        return new GeoChunk.Builder()
                .put(EarthAttachments.ELEVATION, elevation.get(sourceView, outputShape))
                .put(EarthAttachments.LAND_COVER, landCover.get(sourceView, outputShape))
                .put(EarthAttachments.CATION_EXCHANGE_CAPACITY, cationExchangeCapacity.get(sourceView, outputShape))
                .put(EarthAttachments.ORGANIC_CARBON_CONTENT, organicCarbonContent.get(sourceView, outputShape))
                .put(EarthAttachments.SOIL_PH, soilPh.get(sourceView, outputShape))
                .put(EarthAttachments.CLAY_CONTENT, clayContent.get(sourceView, outputShape))
                .put(EarthAttachments.SILT_CONTENT, siltContent.get(sourceView, outputShape))
                .put(EarthAttachments.SAND_CONTENT, sandContent.get(sourceView, outputShape))
                .put(EarthAttachments.SOIL_SUBORDER, soilSuborder.get(sourceView, outputShape))
                .put(EarthAttachments.MEAN_TEMPERATURE, meanTemperature.get(sourceView, outputShape))
                .put(EarthAttachments.MIN_TEMPERATURE, minTemperature.get(sourceView, outputShape))
                .put(EarthAttachments.ANNUAL_RAINFALL, annualRainfall.get(sourceView, outputShape))
                .build();
    }
}
