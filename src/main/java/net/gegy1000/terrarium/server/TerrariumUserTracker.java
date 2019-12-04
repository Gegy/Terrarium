package net.gegy1000.terrarium.server;

import net.gegy1000.gengen.api.GenericWorldType;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumAuxCaps;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
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
public class TerrariumUserTracker {
    private static final Set<EntityPlayer> TERRARIUM_USERS = new HashSet<>();
    private static GenerationSettings providedSettings;

    public static void markPlayerUsingTerrarium(EntityPlayer player) {
        TERRARIUM_USERS.add(player);
    }

    public static void provideSettings(World world, String settings) {
        if (world == null) return;

        TerrariumWorldType worldType = GenericWorldType.unwrapAs(world.getWorldType(), TerrariumWorldType.class);

        PropertyPrototype prototype = worldType.buildPropertyPrototype();
        providedSettings = GenerationSettings.parse(prototype, settings);

        TerrariumAuxCaps aux = world.getCapability(TerrariumCapabilities.aux(), null);
        if (aux == null) return;

        Collection<ICapabilityProvider> capabilities = worldType.createCapabilities(world, providedSettings);
        for (ICapabilityProvider provider : capabilities) {
            aux.addAux(provider);
        }
    }

    public static boolean usesTerrarium(EntityPlayer player) {
        return TERRARIUM_USERS.contains(player);
    }

    public static Set<EntityPlayer> getTerrariumUsers() {
        return new HashSet<>(TERRARIUM_USERS);
    }

    @Nullable
    public static GenerationSettings getProvidedSettings() {
        return providedSettings;
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        TERRARIUM_USERS.remove(event.player);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        TERRARIUM_USERS.removeAll(event.getWorld().playerEntities);
    }
}
