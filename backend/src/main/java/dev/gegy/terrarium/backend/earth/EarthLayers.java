package dev.gegy.terrarium.backend.earth;

import dev.gegy.terrarium.backend.GeoAttachment;
import dev.gegy.terrarium.backend.GeoChunk;
import dev.gegy.terrarium.backend.GeoView;
import dev.gegy.terrarium.backend.earth.cover.Cover;
import dev.gegy.terrarium.backend.earth.soil.SoilSuborder;
import dev.gegy.terrarium.backend.layer.GeoLayer;
import dev.gegy.terrarium.backend.projection.Projection;
import dev.gegy.terrarium.backend.raster.EnumRaster;
import dev.gegy.terrarium.backend.raster.ShortRaster;
import dev.gegy.terrarium.backend.raster.UnsignedByteRaster;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public record EarthLayers(
        GeoLayer<ShortRaster> elevation,
        GeoLayer<EnumRaster<Cover>> landCover,
        GeoLayer<UnsignedByteRaster> soilPh,
        GeoLayer<UnsignedByteRaster> clayContent,
        GeoLayer<UnsignedByteRaster> siltContent,
        GeoLayer<UnsignedByteRaster> sandContent,
        GeoLayer<EnumRaster<SoilSuborder>> soilSuborder,
        Executor executor
) implements GeoLayer<GeoChunk> {
    public static EarthLayers create(final EarthTiles tiles, final Projection projection, final Executor executor) {
        return new EarthLayers(
                projection.createInterpolatedLayer(tiles.elevation(), executor),
                projection.createVoronoiLayer(tiles.landCover(), executor),
                projection.createInterpolatedLayer(tiles.soilPh(), executor),
                projection.createInterpolatedLayer(tiles.clayContent(), executor),
                projection.createInterpolatedLayer(tiles.siltContent(), executor),
                projection.createInterpolatedLayer(tiles.sandContent(), executor),
                projection.createVoronoiLayer(tiles.soilSuborder(), executor),
                executor
        );
    }

    @Override
    public CompletableFuture<Optional<GeoChunk>> get(final GeoView view) {
        return new GeoChunk.Builder()
                .put(GeoAttachment.ELEVATION, elevation.get(view))
                .put(GeoAttachment.LAND_COVER, landCover.get(view))
                .build();
    }
}
