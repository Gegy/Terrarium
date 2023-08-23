package dev.gegy.terrarium.backend.layer;

import dev.gegy.terrarium.backend.GeoView;
import dev.gegy.terrarium.backend.raster.Raster;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface RasterSampler<V extends Raster> {
    CompletableFuture<Optional<V>> get(GeoView view);

    int width();

    int height();
}
