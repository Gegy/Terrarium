package net.gegy1000.terrarium.server

import net.gegy1000.terrarium.server.capability.TerrariumCapabilities
import net.gegy1000.terrarium.server.capability.TerrariumWorldData
import net.gegy1000.terrarium.server.util.Coordinate
import net.gegy1000.terrarium.server.world.EarthGenerationSettings
import net.gegy1000.terrarium.server.world.EarthWorldType
import net.minecraft.world.World
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ServerEventHandler {
    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        val world = event.world
        if (world.worldType == EarthWorldType) {
            val settings = EarthGenerationSettings.deserialize(world.worldInfo.generatorOptions)
            world.spawnPoint = Coordinate.fromLatLng(settings, 27.988350, 86.923641).toBlockPos()
        }
    }

    @SubscribeEvent
    fun onAttachCapabilities(event: AttachCapabilitiesEvent<World>) {
        val world = event.`object`
        if (world.worldType == EarthWorldType) {
            event.addCapability(TerrariumCapabilities.worldDataId, TerrariumWorldData.Implementation(world))
        }
    }
}
