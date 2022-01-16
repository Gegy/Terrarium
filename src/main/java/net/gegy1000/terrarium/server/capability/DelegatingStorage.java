package net.gegy1000.terrarium.server.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public final class DelegatingStorage<T extends INBTSerializable<Nbt>, Nbt extends NBTBase> implements Capability.IStorage<T> {
    private final Class<Nbt> nbtType;

    public DelegatingStorage(Class<Nbt> nbtType) {
        this.nbtType = nbtType;
    }

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
        return instance != null ? instance.serializeNBT() : null;
    }

    @Override
    public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {
        if (this.nbtType.isInstance(nbt)) {
            instance.deserializeNBT(this.nbtType.cast(nbt));
        }
    }
}
