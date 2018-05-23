package net.gegy1000.terrarium.server.world.coordinate;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.event.TerrariumRegistryEvent;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Terrarium.MODID)
public class CoordinateStateRegistry {
    private static final Map<ResourceLocation, InstanceObjectParser<CoordinateState>> STATES = new HashMap<>();

    public static void onInit() {
        MinecraftForge.EVENT_BUS.post(new Event(STATES));
    }

    @SubscribeEvent
    public static void onRegisterConverters(Event event) {
        event.register(new ResourceLocation(Terrarium.MODID, "lat_lng"), (worldData, world, valueParser, objectRoot) -> {
            double worldScale = valueParser.parseDouble(objectRoot, "world_scale");
            double latLngLine = valueParser.parseDouble(objectRoot, "latlng_line");
            return new LatLngCoordinateState(worldScale, latLngLine);
        });

        event.register(new ResourceLocation(Terrarium.MODID, "world_scaled"), (worldData, world, valueParser, objectRoot) -> {
            double worldScale = valueParser.parseDouble(objectRoot, "world_scale");
            double scaleMultiplierX = worldScale;
            double scaleMultiplierZ = worldScale;
            if (objectRoot.has("scale_multiplier")) {
                double scaleMultiplier = valueParser.parseDouble(objectRoot, "scale_multiplier");
                scaleMultiplierX *= scaleMultiplier;
                scaleMultiplierZ *= scaleMultiplier;
            } else if (objectRoot.has("scale_multiplier_x") && objectRoot.has("scale_multiplier_z")) {
                scaleMultiplierX *= valueParser.parseDouble(objectRoot, "scale_multiplier_x");
                scaleMultiplierZ *= valueParser.parseDouble(objectRoot, "scale_multiplier_z");
            }
            return new ScaledCoordinateState(scaleMultiplierX, scaleMultiplierZ);
        });

        event.register(new ResourceLocation(Terrarium.MODID, "none"), (worldData, world, valueParser, objectRoot) -> new ScaledCoordinateState(1.0));
    }

    public static InstanceObjectParser<CoordinateState> get(ResourceLocation key) {
        return STATES.get(key);
    }

    public static Map<ResourceLocation, InstanceObjectParser<CoordinateState>> getRegistry() {
        return Collections.unmodifiableMap(STATES);
    }

    public static final class Event extends TerrariumRegistryEvent<InstanceObjectParser<CoordinateState>> {
        private Event(Map<ResourceLocation, InstanceObjectParser<CoordinateState>> registry) {
            super(registry);
        }
    }
}
