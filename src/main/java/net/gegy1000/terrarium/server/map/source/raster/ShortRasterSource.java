package net.gegy1000.terrarium.server.map.source.raster;

import net.gegy1000.terrarium.server.util.Coordinate;

public interface ShortRasterSource {
    short get(Coordinate coordinate);

    void sampleArea(short[] data, Coordinate minimumCoordinate, Coordinate maximumCoordinate);
}
