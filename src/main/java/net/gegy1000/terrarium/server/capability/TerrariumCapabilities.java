package net.gegy1000.terrarium.server.capability;

import com.google.common.base.Preconditions;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import java.util.concurrent.Callable;

public class TerrariumCapabilities {
    public static final ResourceLocation WORLD_DATA_ID = new ResourceLocation(Terrarium.ID, "world_data");
    public static final ResourceLocation AUX_DATA_ID = new ResourceLocation(Terrarium.ID, "aux_data");

    @CapabilityInject(TerrariumWorld.class)
    private static Capability<TerrariumWorld> world;

    @CapabilityInject(TerrariumAuxCaps.class)
    private static Capability<TerrariumAuxCaps> aux;

    public static void onPreInit() {
        CapabilityManager.INSTANCE.register(TerrariumWorld.class, new VoidStorage<>(), unsupported());
        CapabilityManager.INSTANCE.register(TerrariumAuxCaps.class, new VoidStorage<>(), TerrariumAuxCaps.Implementation::new);
    }

    private static <T> Callable<T> unsupported() {
        return () -> {
            throw new UnsupportedOperationException();
        };
    }

    public static Capability<TerrariumWorld> world() {
        Preconditions.checkNotNull(world, "terrarium world cap not initialized");
        return world;
    }

    public static Capability<TerrariumAuxCaps> aux() {
        Preconditions.checkNotNull(aux, "terrarium aux world cap not initialized");
        return aux;
    }
}
