package dev.gegy.terrarium.map.feature;

import dev.gegy.terrarium.backend.GeoView;
import dev.gegy.terrarium.backend.earth.EarthLayers;
import dev.gegy.terrarium.backend.layer.GeoLayer;
import dev.gegy.terrarium.backend.raster.BitRaster;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public record MaskFeature(
        Function<EarthLayers, GeoLayer<BitRaster>> layerFunction,
        int trueColor,
        int falseColor
) implements RasterMapFeature<BitRaster> {
    @Override
    public CompletableFuture<Optional<BitRaster>> sample(final EarthLayers layers, final GeoView view) {
        return layerFunction.apply(layers).getExact(view);
    }

    @Override
    public int getColor(final BitRaster raster, final int x, final int y) {
        return raster.getBoolean(x, y) ? trueColor : falseColor;
    }
}
