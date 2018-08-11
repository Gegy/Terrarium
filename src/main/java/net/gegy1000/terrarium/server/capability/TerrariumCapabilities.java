package net.gegy1000.terrarium.server.capability;

import net.gegy1000.terrarium.Terrarium;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class TerrariumCapabilities {
    public static final ResourceLocation WORLD_DATA_ID = new ResourceLocation(Terrarium.MODID, "world_data");
    public static final ResourceLocation EXTERNAL_DATA_ID = new ResourceLocation(Terrarium.MODID, "external_data");

    @CapabilityInject(TerrariumWorldData.class)
    public static Capability<TerrariumWorldData> worldDataCapability;

    @CapabilityInject(TerrariumExternalCapProvider.class)
    public static Capability<TerrariumExternalCapProvider> externalProviderCapability;

    public static void onPreInit() {
        CapabilityManager.INSTANCE.register(TerrariumWorldData.class, new BlankStorage<>(), TerrariumWorldData.Implementation.class);
        CapabilityManager.INSTANCE.register(TerrariumExternalCapProvider.class, new BlankStorage<>(), TerrariumExternalCapProvider.Implementation::new);
    }
}
