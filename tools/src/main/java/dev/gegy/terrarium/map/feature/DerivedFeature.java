package dev.gegy.terrarium.map.feature;

import dev.gegy.terrarium.Mapper;
import dev.gegy.terrarium.backend.GeoChunk;
import dev.gegy.terrarium.backend.GeoView;
import dev.gegy.terrarium.backend.earth.EarthAttachments;
import dev.gegy.terrarium.backend.earth.EarthLayers;
import dev.gegy.terrarium.backend.earth.GeoParameters;

import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface DerivedFeature extends MapFeature {
    @Override
    default CompletableFuture<Optional<BufferedImage>> render(final EarthLayers layers, final int tileX, final int tileY, final int zoomLevel, final int x0, final int y0, final int x1, final int y1) {
        return layers.getExact(new GeoView(x0, y0, x1, y1))
                .thenApplyAsync(result -> render(result.orElse(GeoChunk.EMPTY)), Mapper.EXECUTOR);
    }

    private Optional<BufferedImage> render(final GeoChunk chunk) {
        return EarthAttachments.from(chunk).map(attachments -> {
            final int width = attachments.elevation().width();
            final int height = attachments.elevation().height();
            final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            final GeoParameters parameters = new GeoParameters();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    parameters.set(attachments, x, y);
                    image.setRGB(x, y, getColor(parameters));
                }
            }
            return image;
        });
    }

    int getColor(GeoParameters parameters);
}
