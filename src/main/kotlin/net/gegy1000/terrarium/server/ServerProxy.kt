package net.gegy1000.terrarium.server

import net.gegy1000.terrarium.server.map.source.HeightSource
import net.gegy1000.terrarium.server.map.source.OverpassSource
import net.gegy1000.terrarium.server.map.source.TerrariumSource
import net.minecraftforge.common.MinecraftForge
import kotlin.concurrent.thread

open class ServerProxy {
    open fun onPreInit() {
        MinecraftForge.EVENT_BUS.register(ServerEventHandler)

        // TODO: Cache all remote data
        thread(name = "Terrarium Remote Load", start = true, isDaemon = true) {
            OverpassSource.loadQuery()
            TerrariumSource.loadInfo()
            HeightSource.loadHeightPoints()
        }
    }

    open fun onInit() {
    }

    open fun onPostInit() {
    }
}