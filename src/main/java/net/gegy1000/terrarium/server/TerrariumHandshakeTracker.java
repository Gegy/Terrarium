package net.gegy1000.terrarium.server;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Terrarium.MODID)
public class TerrariumHandshakeTracker {
    private static final Set<EntityPlayer> FRIENDLY_PLAYERS = new HashSet<>();
    private static GenerationSettings providedSettings;

    public static void markPlayerFriendly(EntityPlayer player) {
        FRIENDLY_PLAYERS.add(player);
    }

    public static void provideSettings(GenerationSettings settings) {
        providedSettings = settings;
    }

    public static boolean isFriendly(EntityPlayer player) {
        return FRIENDLY_PLAYERS.contains(player);
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
