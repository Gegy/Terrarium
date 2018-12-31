package net.gegy1000.terrarium.server;

import com.mojang.datafixers.Dynamic;
import net.gegy1000.terrarium.api.CustomLevelGenerator;
import net.gegy1000.terrarium.server.event.PlayerEvent;
import net.gegy1000.terrarium.server.event.WorldEvent;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorType;
import net.gegy1000.terrarium.server.world.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.customization.PropertyPrototype;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class TerrariumHandshakeTracker {
    private static final Set<PlayerEntity> FRIENDLY_PLAYERS = new HashSet<>();
    private static GenerationSettings providedSettings;

    public static void register() {
        WorldEvent.UNLOAD.register(world -> FRIENDLY_PLAYERS.removeAll(world.players));
        PlayerEvent.DISCONNECT.register(FRIENDLY_PLAYERS::remove);
    }

    public static void markPlayerFriendly(PlayerEntity player) {
        FRIENDLY_PLAYERS.add(player);
    }

    public static <T> void provideSettings(World world, Dynamic<T> settings) {
        CustomLevelGenerator generatorType = CustomLevelGenerator.unwrap(world.getGeneratorType());
        if (!(generatorType instanceof TerrariumGeneratorType)) {
            return;
        }

        PropertyPrototype prototype = ((TerrariumGeneratorType) generatorType).buildPropertyPrototype();
        providedSettings = GenerationSettings.deserialize(prototype, settings);
    }

    public static boolean isFriendly(PlayerEntity player) {
        return FRIENDLY_PLAYERS.contains(player);
    }

    public static Set<PlayerEntity> getFriends() {
        return new HashSet<>(FRIENDLY_PLAYERS);
    }

    @Nullable
    public static GenerationSettings getProvidedSettings() {
        return providedSettings;
    }
}
