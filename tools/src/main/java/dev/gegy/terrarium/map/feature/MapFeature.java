package dev.gegy.terrarium.map.feature;

import dev.gegy.terrarium.backend.earth.EarthLayers;

import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface MapFeature {
    MapFeature EMPTY = (layers, tileX, tileY, zoomLevel, x0, y0, x1, y1) -> CompletableFuture.completedFuture(Optional.empty());

    CompletableFuture<Optional<BufferedImage>> render(EarthLayers layers, int tileX, int tileY, int zoomLevel, int x0, int y0, int x1, int y1);
}
