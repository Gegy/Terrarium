package net.gegy1000.terrarium.server.capability;

import net.gegy1000.terrarium.Terrarium;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import java.util.concurrent.Callable;

public class TerrariumCapabilities {
    public static final ResourceLocation WORLD_DATA_ID = new ResourceLocation(Terrarium.MODID, "world_data");
    public static final ResourceLocation EXTERNAL_DATA_ID = new ResourceLocation(Terrarium.MODID, "external_data");

    @CapabilityInject(TerrariumWorld.class)
    public static Capability<TerrariumWorld> worldDataCapability;

    @CapabilityInject(TerrariumExternalCapProvider.class)
    public static Capability<TerrariumExternalCapProvider> externalProviderCapability;

    public static void onPreInit() {
        CapabilityManager.INSTANCE.register(TerrariumWorld.class, new VoidStorage<>(), unsupported());
        CapabilityManager.INSTANCE.register(TerrariumExternalCapProvider.class, new VoidStorage<>(), TerrariumExternalCapProvider.Implementation::new);
    }

    private static <T> Callable<T> unsupported() {
        return () -> {
            throw new UnsupportedOperationException();
        };
    }
}
