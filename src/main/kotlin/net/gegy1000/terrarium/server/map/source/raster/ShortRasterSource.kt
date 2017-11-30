package net.gegy1000.terrarium.server.map.source.raster

import net.gegy1000.terrarium.server.util.Coordinate

interface ShortRasterSource {
    fun get(coordinate: Coordinate): Short

    fun sampleArea(result: ShortArray, minimumCoordinate: Coordinate, maximumCoordinate: Coordinate)
}
