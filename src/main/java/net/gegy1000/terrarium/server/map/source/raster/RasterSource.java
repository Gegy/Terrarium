package net.gegy1000.terrarium.server.map.source.raster;

import net.gegy1000.terrarium.server.util.Coordinate;

public interface RasterSource<T> {
    T get(Coordinate coordinate);

    void sampleArea(T[] data, Coordinate coordinate, Coordinate size);
}
