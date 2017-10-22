package net.gegy1000.terrarium.server.map.adapter

import net.gegy1000.terrarium.server.map.glob.GlobType
import net.gegy1000.terrarium.server.map.source.OverpassSource
import net.gegy1000.terrarium.server.world.EarthGenerationSettings

interface RegionAdapter {
    fun adaptGlobcover(settings: EarthGenerationSettings, overpassTile: OverpassSource.Tile, globBuffer: Array<GlobType>, x: Int, y: Int, width: Int, height: Int)
}
