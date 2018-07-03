package net.gegy1000.terrarium.server.event;

import net.gegy1000.terrarium.Terrarium;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Map;

public class TerrariumRegistryEvent<T> extends Event {
    private final Map<ResourceLocation, T> registry;

    protected TerrariumRegistryEvent(Map<ResourceLocation, T> registry) {
        this.registry = registry;
    }

    public final void register(ResourceLocation identifier, T value) {
        if (!this.registry.containsKey(identifier)) {
            this.registry.put(identifier, value);
        } else {
            Terrarium.LOGGER.warn("Attempted to override existing registry entry {}", identifier);
        }
    }
}
