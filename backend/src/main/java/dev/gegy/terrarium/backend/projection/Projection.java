package dev.gegy.terrarium.backend.projection;

import dev.gegy.terrarium.backend.layer.GeoLayer;
import dev.gegy.terrarium.backend.layer.LeveledRasterSampler;
import dev.gegy.terrarium.backend.raster.IntLikeRaster;

import java.util.concurrent.Executor;

public interface Projection {
    double blockX(double lat, double lon);

    double blockZ(double lat, double lon);

    double lat(double blockX, double blockZ);

    double lon(double blockX, double blockZ);

    <V extends IntLikeRaster> GeoLayer<V> createInterpolatedLayer(LeveledRasterSampler<V> leveledSampler, Executor executor);
}
