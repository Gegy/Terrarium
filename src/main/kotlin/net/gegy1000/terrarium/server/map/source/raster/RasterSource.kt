package net.gegy1000.terrarium.server.map.source.raster

import net.gegy1000.terrarium.server.util.Coordinate

interface RasterSource<V> {
    fun get(coordinate: Coordinate): V

    fun sampleArea(result: Array<V>, minimumCoordinate: Coordinate, maximumCoordinate: Coordinate)
}
