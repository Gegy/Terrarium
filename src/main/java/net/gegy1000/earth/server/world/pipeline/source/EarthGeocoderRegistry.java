package net.gegy1000.earth.server.world.pipeline.source;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.source.GeocoderRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = TerrariumEarth.MODID)
public class EarthGeocoderRegistry {
    @SubscribeEvent
    public static void onRegisterGeocoders(GeocoderRegistry.Event event) {
        event.register(new ResourceLocation(TerrariumEarth.MODID, "google_geocoder"), (settings, world, valueParser, objectRoot) -> {
            CoordinateState latLngCoordinate = valueParser.parseCoordinateState(objectRoot, "lat_lng_coordinate");
            return new GoogleGeocoder(latLngCoordinate);
        });
    }
}
