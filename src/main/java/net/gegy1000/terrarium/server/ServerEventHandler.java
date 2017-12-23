package net.gegy1000.terrarium.server;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.gegy1000.terrarium.server.world.EarthWorldType;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Terrarium.MODID)
public class ServerEventHandler {
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld();
        if (world.getWorldType() instanceof EarthWorldType) {
            EarthGenerationSettings settings = EarthGenerationSettings.deserialize(world.getWorldInfo().getGeneratorOptions());
            world.setSpawnPoint(Coordinate.fromLatLng(settings, settings.spawnLatitude, settings.spawnLongitude).toBlockPos(0));
        }
    }

    @SubscribeEvent
    public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        World world = event.getObject();
        if (world.getWorldType() instanceof EarthWorldType) {
            event.addCapability(TerrariumCapabilities.WORLD_DATA_ID, new TerrariumWorldData.Implementation(world));
        }
    }
}
