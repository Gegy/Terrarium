package net.gegy1000.terrarium.server

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import net.gegy1000.terrarium.server.map.source.TerrariumSource
import net.gegy1000.terrarium.server.map.source.HeightSource
import net.minecraftforge.common.MinecraftForge

open class ServerProxy {
    open fun onPreInit() {
        MinecraftForge.EVENT_BUS.register(ServerEventHandler)

        launch(CommonPool) {
            TerrariumSource.loadInfo()
            HeightSource.loadHeightPoints()
        }
    }

    open fun onInit() {
    }

    open fun onPostInit() {
    }
}