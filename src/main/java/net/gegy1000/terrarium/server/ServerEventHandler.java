package net.gegy1000.terrarium.server;

import com.google.gson.JsonSyntaxException;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.minecraft.world.DimensionType;
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
        if (ServerEventHandler.shouldHandle(world)) {
            TerrariumWorldData worldData = ((TerrariumWorldType) world.getWorldType()).getWorldData(world);
            if (worldData != null) {
                Coordinate spawnPosition = worldData.getSpawnPosition();
                if (spawnPosition != null) {
                    world.setSpawnPoint(spawnPosition.toBlockPos());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        World world = event.getObject();
        if (ServerEventHandler.shouldHandle(world)) {
            try {
                TerrariumWorldType worldType = (TerrariumWorldType) world.getWorldType();
                event.addCapability(TerrariumCapabilities.WORLD_DATA_ID, new TerrariumWorldData.Implementation(world, worldType));
            } catch (JsonSyntaxException e) {
                Terrarium.LOGGER.error("Failed to construct generator", e);
            }
        }
    }

    private static boolean shouldHandle(World world) {
        return !world.isRemote && world.provider.getDimensionType() == DimensionType.OVERWORLD && world.getWorldType() instanceof TerrariumWorldType;
    }
}
