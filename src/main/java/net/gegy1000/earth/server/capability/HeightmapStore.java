package net.gegy1000.earth.server.capability;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.gengen.api.HeightFunction;
import net.gegy1000.gengen.api.Heightmap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class HeightmapStore implements ICapabilitySerializable<NBTTagCompound> {
    private Heightmap heightmap;

    public static HeightFunction global(World world, int defaultValue) {
        return (x, z) -> {
            Chunk column = world.getChunk(x >> 4, z >> 4);
            HeightmapStore store = column.getCapability(TerrariumEarth.heightmapCap(), null);
            if (store != null && store.heightmap != null) {
                return store.heightmap.get(x & 15, z & 15);
            }
            return defaultValue;
        };
    }

    public void set(Heightmap heightmap) {
        this.heightmap = heightmap;
    }

    public int get(int x, int z) {
        if (this.heightmap == null) return 0;
        return this.heightmap.get(x & 15, z & 15);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == TerrariumEarth.heightmapCap();
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == TerrariumEarth.heightmapCap() ? TerrariumEarth.heightmapCap().cast(this) : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        if (this.heightmap != null) {
            nbt.setIntArray("buffer", this.heightmap.getBuffer());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("buffer", Constants.NBT.TAG_INT_ARRAY)) {
            this.heightmap = Heightmap.wrap(nbt.getIntArray("buffer"));
        }
    }
}
