package net.gegy1000.terrarium.server

import net.gegy1000.terrarium.server.util.Coordinates
import net.gegy1000.terrarium.server.world.EarthGenerationSettings
import net.gegy1000.terrarium.server.world.EarthWorldType
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ServerEventHandler {
    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        val world = event.world
        if (world.worldType is EarthWorldType) {
            val settings = EarthGenerationSettings.deserialize(world.worldInfo.generatorOptions)
            val latitude = -0.598293
            val longitude = -51.527155
            world.spawnPoint = Coordinates.toBlockPos(latitude, longitude, settings)
        }
    }
}