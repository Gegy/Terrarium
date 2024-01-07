package dev.gegy.terrarium.backend.layer;

import dev.gegy.terrarium.backend.GeoView;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface GeoLayer<V> {
    CompletableFuture<Optional<V>> get(GeoView view);
}
