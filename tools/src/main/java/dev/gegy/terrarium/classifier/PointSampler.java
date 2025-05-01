package dev.gegy.terrarium.classifier;

import dev.gegy.terrarium.backend.GeoChunk;
import dev.gegy.terrarium.backend.GeoView;
import dev.gegy.terrarium.backend.earth.EarthAttachments;
import dev.gegy.terrarium.backend.earth.EarthTiles;
import dev.gegy.terrarium.backend.earth.GeoParameters;
import dev.gegy.terrarium.backend.layer.LeveledRasterSampler;
import dev.gegy.terrarium.backend.layer.RasterSampler;
import dev.gegy.terrarium.backend.raster.Raster;
import dev.gegy.terrarium.backend.util.Util;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PointSampler {
    private final EarthTiles tiles;

    public PointSampler(final EarthTiles tiles) {
        this.tiles = tiles;
    }

    public CompletableFuture<Optional<GeoParameters>> sample(final double latitude, final double longitude) {
        return loadAll(latitude, longitude).thenApply(result -> result.flatMap(EarthAttachments::from).map(chunk -> {
            final GeoParameters parameters = new GeoParameters();
            parameters.set(chunk, 0, 0);
            return parameters;
        }));
    }

    private CompletableFuture<Optional<GeoChunk>> loadAll(final double latitude, final double longitude) {
        return new GeoChunk.Builder()
                .put(EarthAttachments.ELEVATION, load(tiles.elevation(), latitude, longitude))
                .put(EarthAttachments.LAND_COVER, load(tiles.landCover(), latitude, longitude))
                .put(EarthAttachments.CATION_EXCHANGE_CAPACITY, load(tiles.cationExchangeCapacity(), latitude, longitude))
                .put(EarthAttachments.ORGANIC_CARBON_CONTENT, load(tiles.organicCarbonContent(), latitude, longitude))
                .put(EarthAttachments.SOIL_PH, load(tiles.soilPh(), latitude, longitude))
                .put(EarthAttachments.CLAY_CONTENT, load(tiles.clayContent(), latitude, longitude))
                .put(EarthAttachments.SILT_CONTENT, load(tiles.siltContent(), latitude, longitude))
                .put(EarthAttachments.SAND_CONTENT, load(tiles.sandContent(), latitude, longitude))
                .put(EarthAttachments.SOIL_SUBORDER, load(tiles.soilSuborder(), latitude, longitude))
                .put(EarthAttachments.MEAN_TEMPERATURE, load(tiles.climateSamplers().meanTemperature(), latitude, longitude))
                .put(EarthAttachments.MIN_TEMPERATURE, load(tiles.climateSamplers().minTemperature(), latitude, longitude))
                .put(EarthAttachments.ANNUAL_RAINFALL, load(tiles.climateSamplers().annualRainfall(), latitude, longitude))
                .build();
    }

    private <V extends Raster> CompletableFuture<Optional<V>> load(final LeveledRasterSampler<V> leveledSampler, final double latitude, final double longitude) {
        final RasterSampler<V> sampler = leveledSampler.maxLevel();
        final int x = Util.floorInt((longitude + 180.0) / 360.0 * sampler.width());
        final int z = Util.floorInt((90.0 - latitude) / 180.0 * sampler.height());
        return sampler.get(new GeoView(x, z, x, z));
    }
}
