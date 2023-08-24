package dev.gegy.terrarium;

import dev.gegy.terrarium.backend.GeoView;
import dev.gegy.terrarium.backend.earth.EarthLayers;
import dev.gegy.terrarium.backend.earth.climate.RainfallRaster;
import dev.gegy.terrarium.backend.earth.climate.TemperatureRaster;
import dev.gegy.terrarium.backend.layer.GeoLayer;

import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public record RainfallFeature(
        Function<EarthLayers, GeoLayer<RainfallRaster>> layerFunction
) implements MapFeature {
    @Override
    public CompletableFuture<Optional<BufferedImage>> render(final EarthLayers layers, final int zoomLevel, final int x0, final int y0, final int x1, final int y1) {
        return layerFunction.apply(layers).get(new GeoView(x0, y0, x1, y1))
                .thenApplyAsync(result -> result.map(this::render), Map.EXECUTOR);
    }

    private BufferedImage render(final RainfallRaster raster) {
        final BufferedImage image = new BufferedImage(raster.width(), raster.height(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < raster.height(); y++) {
            for (int x = 0; x < raster.width(); x++) {
                image.setRGB(x, y, ColorRamps.RAINFALL.get(raster.getRainfall(x, y)));
            }
        }
        return image;
    }
}
