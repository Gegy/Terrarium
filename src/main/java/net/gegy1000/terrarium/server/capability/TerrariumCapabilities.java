package net.gegy1000.terrarium.server.capability;

import net.gegy1000.terrarium.Terrarium;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class TerrariumCapabilities {
    public static final ResourceLocation WORLD_DATA_ID = new ResourceLocation(Terrarium.MODID, "world_data");

    @CapabilityInject(TerrariumWorldData.class)
    public static Capability<TerrariumWorldData> worldDataCapability;

    public static void onPreInit() {
        CapabilityManager.INSTANCE.register(TerrariumWorldData.class, new AbsentStorage<>(), TerrariumWorldData.Implementation.class);
    }

    private static class AbsentStorage<T> implements Capability.IStorage<T> {
        @Nullable
        @Override
        public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
            return null;
        }

        @Override
        public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {
        }
    }
}
