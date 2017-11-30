package net.gegy1000.terrarium.server.capability

import net.gegy1000.terrarium.Terrarium
import net.minecraft.nbt.NBTBase
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager

object TerrariumCapabilities {
    val worldDataId = ResourceLocation(Terrarium.MODID, "world_data")

    @CapabilityInject(TerrariumWorldData::class)
    lateinit var worldDataCapability: Capability<TerrariumWorldData>

    fun onPreInit() {
        CapabilityManager.INSTANCE.register(TerrariumWorldData::class.java, AbsentStorage<TerrariumWorldData>(), TerrariumWorldData.Implementation::class.java)
    }

    private class AbsentStorage<T> : Capability.IStorage<T> {
        override fun writeNBT(capability: Capability<T>, instance: T, side: EnumFacing) = null

        override fun readNBT(capability: Capability<T>, instance: T, side: EnumFacing, nbt: NBTBase) = Unit
    }
}
