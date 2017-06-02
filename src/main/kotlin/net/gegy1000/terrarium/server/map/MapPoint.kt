package net.gegy1000.terrarium.server.map

import net.minecraft.entity.Entity

data class MapPoint(val latitude: Double, val longitude: Double) {
    val x: Double
        get() = this.longitude * 1200.0
    val z: Double
        get() = -this.latitude * 1200.0

    companion object {
        fun entity(entity: Entity) = this.world(entity.posX, entity.posZ)

        fun world(x: Int, z: Int) = MapPoint(-z / 1200.0, x / 1200.0)

        fun world(x: Double, z: Double) = MapPoint(-z / 1200.0, x / 1200.0)
    }
}
