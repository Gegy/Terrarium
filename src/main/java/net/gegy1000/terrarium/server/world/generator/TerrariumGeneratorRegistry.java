package net.gegy1000.terrarium.server.world.generator;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.event.TerrariumRegistryEvent;
import net.gegy1000.terrarium.server.util.JsonDiscoverer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Terrarium.MODID)
public class TerrariumGeneratorRegistry {
    private static final BiMap<ResourceLocation, TerrariumGenerator> GENERATORS = HashBiMap.create();

    public static final TerrariumGenerator DEFAULT = new TerrariumGenerator.Default();

    private static final JsonDiscoverer<JsonTerrariumGenerator> DISCOVERER = new JsonDiscoverer<>(JsonTerrariumGenerator::parseGenerator);

    public static void onInit() {
        MinecraftForge.EVENT_BUS.post(new Event(GENERATORS));
    }

    @SubscribeEvent
    public static void onRegisterGenerators(Event event) {
        List<JsonDiscoverer.Result<JsonTerrariumGenerator>> discoveredPresets = DISCOVERER.discoverFiles("data", "generation_system");

        for (JsonDiscoverer.Result<JsonTerrariumGenerator> result : discoveredPresets) {
            JsonTerrariumGenerator generator = result.getParsed();
            event.register(generator.getIdentifier(), generator);
        }
    }

    public static TerrariumGenerator get(ResourceLocation identifier) {
        return GENERATORS.get(identifier);
    }

    public static ResourceLocation getIdentifier(TerrariumGenerator generator) {
        return GENERATORS.inverse().get(generator);
    }

    public static Map<ResourceLocation, TerrariumGenerator> getRegistry() {
        return Collections.unmodifiableMap(GENERATORS);
    }

    public static final class Event extends TerrariumRegistryEvent<TerrariumGenerator> {
        private Event(Map<ResourceLocation, TerrariumGenerator> registry) {
            super(registry);
        }
    }
}
