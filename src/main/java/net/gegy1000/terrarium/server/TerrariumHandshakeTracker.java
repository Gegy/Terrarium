package net.gegy1000.terrarium.server;

import net.gegy1000.gengen.api.GenericWorldType;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumExternalCapProvider;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.generator.customization.PropertyPrototype;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Terrarium.MODID)
public class TerrariumHandshakeTracker {
    private static final Set<EntityPlayer> FRIENDLY_PLAYERS = new HashSet<>();
    private static GenerationSettings providedSettings;

    public static void markPlayerFriendly(EntityPlayer player) {
        FRIENDLY_PLAYERS.add(player);
    }

    public static void provideSettings(World world, String settings) {
        if (world == null) {
            return;
        }

        GenericWorldType worldType = GenericWorldType.unwrap(world.getWorldType());
        if (!(worldType instanceof TerrariumWorldType)) {
            return;
        }

        PropertyPrototype prototype = ((TerrariumWorldType) worldType).buildPropertyPrototype();
        providedSettings = GenerationSettings.parse(prototype, settings);

        TerrariumExternalCapProvider external = world.getCapability(TerrariumCapabilities.externalProviderCapability, null);
        if (external == null) {
            return;
        }

        Collection<ICapabilityProvider> capabilities = ((TerrariumWorldType) worldType).createCapabilities(world, providedSettings);
        for (ICapabilityProvider provider : capabilities) {
            external.addExternal(provider);
        }
    }

    public static boolean isFriendly(EntityPlayer player) {
        return FRIENDLY_PLAYERS.contains(player);
    }

    public static Set<EntityPlayer> getFriends() {
        return new HashSet<>(FRIENDLY_PLAYERS);
    }

    @Nullable
    public static GenerationSettings getProvidedSettings() {
        return providedSettings;
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        FRIENDLY_PLAYERS.remove(event.player);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        FRIENDLY_PLAYERS.removeAll(event.getWorld().playerEntities);
    }
}
