package net.gegy1000.earth.server.world.compatibility.capability;

import dev.gegy.gengen.core.GenGen;
import net.gegy1000.earth.TerrariumEarth;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = TerrariumEarth.ID)
public final class CcColumnCompatibilityMetadata implements ICapabilitySerializable<NBTTagByte> {
    public static final ResourceLocation ID = new ResourceLocation(TerrariumEarth.ID, "cc_compat_meta");

    private boolean runGenerator;

    public boolean tryRunGenerator() {
        if (!this.runGenerator) {
            this.runGenerator = true;
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    public static CcColumnCompatibilityMetadata get(Chunk chunk) {
        return chunk.getCapability(TerrariumEarth.ccCompatibilityGenerationCap(), null);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == TerrariumEarth.ccCompatibilityGenerationCap();
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == TerrariumEarth.ccCompatibilityGenerationCap() ? TerrariumEarth.ccCompatibilityGenerationCap().cast(this) : null;
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Chunk> event) {
        World world = event.getObject().getWorld();
        if (GenGen.isCubic(world)) {
            event.addCapability(ID, new CcColumnCompatibilityMetadata());
        }
    }

    @Override
    public NBTTagByte serializeNBT() {
        return new NBTTagByte((byte) (this.runGenerator ? 1 : 0));
    }

    @Override
    public void deserializeNBT(NBTTagByte nbt) {
        this.runGenerator = nbt.getByte() != 0;
    }
}
