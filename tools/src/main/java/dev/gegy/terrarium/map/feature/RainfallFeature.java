package dev.gegy.terrarium.map.feature;

import dev.gegy.terrarium.ColorRamps;
import dev.gegy.terrarium.backend.GeoView;
import dev.gegy.terrarium.backend.earth.EarthLayers;
import dev.gegy.terrarium.backend.earth.climate.RainfallRaster;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public record RainfallFeature() implements RasterMapFeature<RainfallRaster> {
    @Override
    public CompletableFuture<Optional<RainfallRaster>> sample(final EarthLayers layers, final GeoView view) {
        return layers.annualRainfall().getExact(view);
    }

    @Override
    public int getColor(final RainfallRaster raster, final int x, final int y) {
        return ColorRamps.RAINFALL.get(raster.getRainfall(x, y));
    }
}
