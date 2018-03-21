package net.gegy1000.terrarium.server.world.bundle;

import net.gegy1000.terrarium.Terrarium;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public interface IdBundle {
    String getIdentifier();

    Collection<ResourceLocation> getEntries();

    default <T> Collection<T> getRegistryEntries(Map<ResourceLocation, T> registry) {
        return this.getEntries().stream().map(key -> {
            T entry = registry.get(key);
            if (entry == null) {
                Terrarium.LOGGER.error("Found invalid registry entry in bundle {}: {}", this.getIdentifier(), key);
            }
            return entry;
        }).collect(Collectors.toList());
    }
}
