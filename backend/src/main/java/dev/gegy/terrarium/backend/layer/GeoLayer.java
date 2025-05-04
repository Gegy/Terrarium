package dev.gegy.terrarium.backend.layer;

import dev.gegy.terrarium.backend.GeoView;
import dev.gegy.terrarium.backend.raster.RasterShape;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface GeoLayer<V> {
    CompletableFuture<Optional<V>> get(GeoView sourceView, RasterShape outputShape);

    default CompletableFuture<Optional<V>> getExact(final GeoView view) {
        return get(view, view.shape());
    }
}
