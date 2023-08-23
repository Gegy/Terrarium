package dev.gegy.terrarium.backend.projection.cylindrical;

import dev.gegy.terrarium.backend.GeoView;
import dev.gegy.terrarium.backend.raster.Raster;

public interface Resampler<R extends Raster> {
    <V extends R> void resample(V source, V target, float scaleX, float scaleY, float offsetX, float offsetY, int seedX, int seedY);

    GeoView extend(GeoView view);
}
