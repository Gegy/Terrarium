package net.gegy1000.terrarium.client

import net.gegy1000.terrarium.server.ServerProxy
import net.minecraftforge.common.MinecraftForge

class ClientProxy : ServerProxy() {
    override fun onPreInit() {
        super.onPreInit()

        MinecraftForge.EVENT_BUS.register(ClientEventHandler)
    }

    override fun onInit() {
        super.onInit()
    }

    override fun onPostInit() {
        super.onPostInit()
    }
}