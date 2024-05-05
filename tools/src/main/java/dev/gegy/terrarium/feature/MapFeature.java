package dev.gegy.terrarium.feature;

import dev.gegy.terrarium.backend.earth.EarthLayers;

import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface MapFeature {
    CompletableFuture<Optional<BufferedImage>> render(EarthLayers layers, int zoomLevel, int x0, int y0, int x1, int y1);
}
