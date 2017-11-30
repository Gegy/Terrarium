package net.gegy1000.terrarium.server.map.source.osm

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import net.gegy1000.terrarium.server.map.source.tiled.TiledDataAccess

class OverpassTileAccess(val elements: Set<OverpassSource.Element> = hashSetOf()) : TiledDataAccess {
    val nodes = Int2ObjectArrayMap<OverpassSource.Element>()

    init {
        this.elements.filter { it.type == "node" }.forEach { this.nodes.put(it.id, it) }
    }
}
