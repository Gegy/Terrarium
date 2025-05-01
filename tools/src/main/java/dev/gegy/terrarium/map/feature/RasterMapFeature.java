package dev.gegy.terrarium.map.feature;

import dev.gegy.terrarium.Mapper;
import dev.gegy.terrarium.backend.GeoView;
import dev.gegy.terrarium.backend.earth.EarthLayers;
import dev.gegy.terrarium.backend.raster.Raster;

import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface RasterMapFeature<R extends Raster> extends MapFeature {
    CompletableFuture<Optional<R>> sample(EarthLayers layers, GeoView view);

    int getColor(R raster, int x, int y);

    @Override
    default CompletableFuture<Optional<BufferedImage>> render(final EarthLayers layers, final int tileX, final int tileY, final int zoomLevel, final int x0, final int y0, final int x1, final int y1) {
        return sample(layers, new GeoView(x0, y0, x1, y1)).thenApplyAsync(result -> result.map(raster -> {
            final BufferedImage image = new BufferedImage(raster.width(), raster.height(), BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < raster.height(); y++) {
                for (int x = 0; x < raster.width(); x++) {
                    image.setRGB(x, y, getColor(raster, x, y));
                }
            }
            return image;
        }), Mapper.EXECUTOR);
    }
}
