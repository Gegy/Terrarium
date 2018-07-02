package net.gegy1000.terrarium.server.world.generator.customization;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.event.TerrariumRegistryEvent;
import net.gegy1000.terrarium.server.util.JsonDiscoverer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Terrarium.MODID)
public class TerrariumPresetRegistry {
    private static final Map<ResourceLocation, TerrariumPreset> PRESETS = new HashMap<>();

    private static final JsonDiscoverer<TerrariumPreset> DISCOVERER = new JsonDiscoverer<>(TerrariumPreset::parse);

    public static void onInit() {
        MinecraftForge.EVENT_BUS.post(new Event(PRESETS));
    }

    @SubscribeEvent
    public static void onRegisterPresets(Event event) {
        List<JsonDiscoverer.Result<TerrariumPreset>> discoveredPresets = DISCOVERER.discoverFiles("data", "terrarium/presets");

        for (JsonDiscoverer.Result<TerrariumPreset> result : discoveredPresets) {
            event.register(result.getKey(), result.getParsed());
        }
    }

    public static TerrariumPreset get(ResourceLocation identifier) {
        return PRESETS.get(identifier);
    }

    public static Collection<TerrariumPreset> getPresets() {
        return PRESETS.values();
    }

    public static Map<ResourceLocation, TerrariumPreset> getRegistry() {
        return Collections.unmodifiableMap(PRESETS);
    }

    public static final class Event extends TerrariumRegistryEvent<TerrariumPreset> {
        private Event(Map<ResourceLocation, TerrariumPreset> registry) {
            super(registry);
        }
    }
}
