package dev.gegy.terrarium.backend.layer;

import dev.gegy.terrarium.backend.raster.Raster;

import java.util.List;

public class LeveledRasterSampler<V extends Raster> {
    private final List<RasterSampler<V>> levels;

    public LeveledRasterSampler(final List<RasterSampler<V>> levels) {
        this.levels = List.copyOf(levels);
    }

    @SafeVarargs
    public LeveledRasterSampler(final RasterSampler<V>... levels) {
        this.levels = List.of(levels);
    }
}
