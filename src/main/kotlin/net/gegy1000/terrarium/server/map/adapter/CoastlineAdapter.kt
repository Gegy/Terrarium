package net.gegy1000.terrarium.server.map.adapter

import net.gegy1000.terrarium.server.map.glob.GlobType
import net.gegy1000.terrarium.server.map.source.OverpassSource.Tile
import net.gegy1000.terrarium.server.util.Coordinate
import net.gegy1000.terrarium.server.util.Interpolation
import net.gegy1000.terrarium.server.world.EarthGenerationSettings

object CoastlineAdapter : RegionAdapter {
    override fun adaptGlobcover(settings: EarthGenerationSettings, overpassTile: Tile, globBuffer: Array<GlobType>, x: Int, y: Int, width: Int, height: Int) {
        val coastlines = overpassTile.elements.filter { it.isType("natural", "coastline") }

        coastlines.forEach {
            val nodes = it.nodes.map { overpassTile.nodes[it] }

            for (i in 1..nodes.lastIndex) {
                val current = nodes[i - 1]
                val next = nodes[i]

                val currentCoordinate = Coordinate.fromLatLng(settings, current.latitude, current.longitude)
                val nextCoordinate = Coordinate.fromLatLng(settings, next.latitude, next.longitude)

                val originX = currentCoordinate.blockX
                val originY = currentCoordinate.blockZ
                val targetX = nextCoordinate.blockX
                val targetY = nextCoordinate.blockZ

                Interpolation.interpolateLine(originX, originY, targetX, targetY, true) { point ->
                }
            }
        }
    }
}
