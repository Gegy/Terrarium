package net.gegy1000.terrarium.server.item

import net.gegy1000.terrarium.Terrarium
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.registry.GameRegistry

object ItemRegistry {
    val TRACKER = TrackerItem()

    fun register() {
        GameRegistry.register(TRACKER, ResourceLocation(Terrarium.MODID, "tracker"))
    }
}
