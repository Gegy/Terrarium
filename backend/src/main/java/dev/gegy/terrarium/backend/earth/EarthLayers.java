package dev.gegy.terrarium.backend.earth;

import dev.gegy.terrarium.backend.GeoAttachment;
import dev.gegy.terrarium.backend.GeoChunk;
import dev.gegy.terrarium.backend.GeoView;
import dev.gegy.terrarium.backend.layer.GeoLayer;
import dev.gegy.terrarium.backend.projection.Projection;
import dev.gegy.terrarium.backend.raster.ShortRaster;
import dev.gegy.terrarium.backend.raster.UnsignedByteRaster;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public record EarthLayers(
        GeoLayer<ShortRaster> elevation,
        GeoLayer<UnsignedByteRaster> soilPh,
        GeoLayer<UnsignedByteRaster> clayContent,
        GeoLayer<UnsignedByteRaster> siltContent,
        GeoLayer<UnsignedByteRaster> sandContent,
        Executor executor
) implements GeoLayer<GeoChunk> {
    public static EarthLayers create(final EarthTiles tiles, final Projection projection, final Executor executor) {
        return new EarthLayers(
                projection.createInterpolatedLayer(tiles.elevation(), executor),
                projection.createInterpolatedLayer(tiles.soilPh(), executor),
                projection.createInterpolatedLayer(tiles.clayContent(), executor),
                projection.createInterpolatedLayer(tiles.siltContent(), executor),
                projection.createInterpolatedLayer(tiles.sandContent(), executor),
                executor
        );
    }

    @Override
    public CompletableFuture<Optional<GeoChunk>> get(final GeoView view) {
        return new GeoChunk.Builder()
                .put(GeoAttachment.ELEVATION, elevation.get(view))
                .build();
    }
}
