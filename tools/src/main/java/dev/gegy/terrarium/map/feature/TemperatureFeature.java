package dev.gegy.terrarium.map.feature;

import dev.gegy.terrarium.ColorRamps;
import dev.gegy.terrarium.backend.GeoView;
import dev.gegy.terrarium.backend.earth.EarthLayers;
import dev.gegy.terrarium.backend.earth.climate.TemperatureRaster;
import dev.gegy.terrarium.backend.layer.GeoLayer;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public record TemperatureFeature(
        Function<EarthLayers, GeoLayer<TemperatureRaster>> layerFunction
) implements RasterMapFeature<TemperatureRaster> {
    @Override
    public CompletableFuture<Optional<TemperatureRaster>> sample(final EarthLayers layers, final GeoView view) {
        return layerFunction.apply(layers).getExact(view);
    }

    @Override
    public int getColor(final TemperatureRaster raster, final int x, final int y) {
        return ColorRamps.TEMPERATURE.get(raster.getTemperature(x, y));
    }
}
