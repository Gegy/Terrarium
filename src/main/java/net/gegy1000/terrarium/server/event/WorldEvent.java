package net.gegy1000.terrarium.server.event;

import net.fabricmc.fabric.util.HandlerList;
import net.fabricmc.fabric.util.HandlerRegistry;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class WorldEvent {
    public static final HandlerRegistry<Consumer<World>> LOAD = new HandlerList<>(Consumer.class);
    public static final HandlerRegistry<Consumer<World>> UNLOAD = new HandlerList<>(Consumer.class);

    private WorldEvent() {
    }

    public static void dispatch(HandlerRegistry<Consumer<World>> registry, World world) {
        HandlerList<Consumer<World>> handlerList = (HandlerList<Consumer<World>>) registry;
        for (Consumer<World> handler : handlerList.getBackingArray()) {
            handler.accept(world);
        }
    }
}
