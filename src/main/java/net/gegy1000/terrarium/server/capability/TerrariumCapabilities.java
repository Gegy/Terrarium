package net.gegy1000.terrarium.server.capability;

import com.google.common.base.Preconditions;
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
    private static Capability<TerrariumWorld> world;

    @CapabilityInject(TerrariumExternalCapProvider.class)
    private static Capability<TerrariumExternalCapProvider> external;

    public static void onPreInit() {
        CapabilityManager.INSTANCE.register(TerrariumWorld.class, new VoidStorage<>(), unsupported());
        CapabilityManager.INSTANCE.register(TerrariumExternalCapProvider.class, new VoidStorage<>(), TerrariumExternalCapProvider.Implementation::new);
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

    public static Capability<TerrariumExternalCapProvider> external() {
        Preconditions.checkNotNull(external, "terrarium external world cap not initialized");
        return external;
    }
}
