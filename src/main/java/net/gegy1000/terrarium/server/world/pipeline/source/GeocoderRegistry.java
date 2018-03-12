package net.gegy1000.terrarium.server.world.pipeline.source;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.event.TerrariumRegistryEvent;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.pipeline.source.earth.GoogleGeocoder;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Terrarium.MODID)
public class GeocoderRegistry {
    private static final Map<ResourceLocation, InstanceObjectParser<Geocoder>> GEOCODERS = new HashMap<>();

    public static void onInit() {
        MinecraftForge.EVENT_BUS.post(new Event(GEOCODERS));
    }

    @SubscribeEvent
    public static void onRegisterGeocoders(Event event) {
        event.register(new ResourceLocation(Terrarium.MODID, "google_geocoder"), (settings, world, valueParser, objectRoot) -> {
            CoordinateState latLngCoordinate = valueParser.parseCoordinateState(objectRoot, "lat_lng_coordinate");
            return new GoogleGeocoder(latLngCoordinate);
        });
    }

    public static InstanceObjectParser<Geocoder> getGeocoder(ResourceLocation identifier) {
        return GEOCODERS.get(identifier);
    }

    public static final class Event extends TerrariumRegistryEvent<InstanceObjectParser<Geocoder>> {
        private Event(Map<ResourceLocation, InstanceObjectParser<Geocoder>> registry) {
            super(registry);
        }
    }
}
