package net.gegy1000.terrarium.client

import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.server.ServerProxy
import net.gegy1000.terrarium.server.item.ItemRegistry
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraftforge.client.model.ModelLoader

class ClientProxy : ServerProxy() {
    override fun onPreInit() {
        super.onPreInit()

        ModelLoader.setCustomModelResourceLocation(ItemRegistry.TRACKER, 0, ModelResourceLocation("${Terrarium.MODID}:tracker", "inventory"))
    }

    override fun onInit() {
        super.onInit()
    }

    override fun onPostInit() {
        super.onPostInit()
    }
}