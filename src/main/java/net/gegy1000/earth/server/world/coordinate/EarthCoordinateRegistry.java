package net.gegy1000.earth.server.world.coordinate;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateStateRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = TerrariumEarth.MODID)
public class EarthCoordinateRegistry {
    @SubscribeEvent
    public static void onRegisterCoordinateStates(CoordinateStateRegistry.Event event) {
        event.register(new ResourceLocation(TerrariumEarth.MODID, "debug_lat_lng"), (worldData, world, valueParser, objectRoot) -> new DebugLatLngCoordinateState());
    }
}
