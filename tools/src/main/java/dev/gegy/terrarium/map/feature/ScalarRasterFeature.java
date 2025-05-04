package dev.gegy.terrarium.map.feature;

import dev.gegy.terrarium.ColorRamp;
import dev.gegy.terrarium.backend.GeoView;
import dev.gegy.terrarium.backend.earth.EarthLayers;
import dev.gegy.terrarium.backend.layer.GeoLayer;
import dev.gegy.terrarium.backend.raster.IntLikeRaster;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public record ScalarRasterFeature(
        Function<EarthLayers, GeoLayer<? extends IntLikeRaster>> layerFunction,
        ColorRamp colorRamp
) implements RasterMapFeature<IntLikeRaster> {
    @Override
    public CompletableFuture<Optional<IntLikeRaster>> sample(final EarthLayers layers, final GeoView view) {
        return layerFunction.apply(layers).getExact(view).thenApply(r -> r.map(Function.identity()));
    }

    @Override
    public int getColor(final IntLikeRaster raster, final int x, final int y) {
        return colorRamp.get(raster.getInt(x, y));
    }
}
