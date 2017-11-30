package net.gegy1000.terrarium.server

import net.gegy1000.terrarium.server.capability.TerrariumCapabilities
import net.gegy1000.terrarium.server.map.source.TerrariumData
import net.gegy1000.terrarium.server.map.source.height.HeightSource
import net.minecraftforge.common.MinecraftForge
import kotlin.concurrent.thread

open class ServerProxy {
    open fun onPreInit() {
        MinecraftForge.EVENT_BUS.register(ServerEventHandler)
        TerrariumCapabilities.onPreInit()

        // TODO: Cache all remote data
        thread(name = "Terrarium Remote Load", start = true, isDaemon = true) {
            TerrariumData.loadInfo()
            HeightSource.loadValidTiles()
        }
    }

    open fun onInit() {
    }

    open fun onPostInit() {
    }
}
