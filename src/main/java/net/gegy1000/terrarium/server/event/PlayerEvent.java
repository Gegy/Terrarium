package net.gegy1000.terrarium.server.event;

import net.fabricmc.fabric.util.HandlerList;
import net.fabricmc.fabric.util.HandlerRegistry;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Consumer;

public class PlayerEvent {
    public static final HandlerRegistry<Consumer<ServerPlayerEntity>> CONNECT = new HandlerList<>(Consumer.class);
    public static final HandlerRegistry<Consumer<ServerPlayerEntity>> DISCONNECT = new HandlerList<>(Consumer.class);

    private PlayerEvent() {
    }

    public static void dispatch(HandlerRegistry<Consumer<ServerPlayerEntity>> registry, ServerPlayerEntity player) {
        HandlerList<Consumer<ServerPlayerEntity>> handlerList = (HandlerList<Consumer<ServerPlayerEntity>>) registry;
        for (Consumer<ServerPlayerEntity> handler : handlerList.getBackingArray()) {
            handler.accept(player);
        }
    }
}
