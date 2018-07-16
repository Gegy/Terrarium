package net.gegy1000.terrarium.server;

import com.google.gson.JsonSyntaxException;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = Terrarium.MODID)
public class ServerEventHandler {
    private static final long REGION_TRACK_INTERVAL = 1000;
    private static long lastRegionTrackTime;

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld();
        if (!world.isRemote && ServerEventHandler.shouldHandle(world)) {
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
    public static void onWorldUnload(WorldEvent.Unload event) {
        World world = event.getWorld();
        if (!world.isRemote && ServerEventHandler.shouldHandle(world)) {
            TerrariumWorldData worldData = ((TerrariumWorldType) world.getWorldType()).getWorldData(world);
            if (worldData != null) {
                worldData.getRegionHandler().close();
            }
        }
    }

    @SubscribeEvent
    public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        World world = event.getObject();
        if (!world.isRemote && ServerEventHandler.shouldHandle(world)) {
            try {
                TerrariumWorldType worldType = (TerrariumWorldType) world.getWorldType();
                event.addCapability(TerrariumCapabilities.WORLD_DATA_ID, new TerrariumWorldData.Implementation(world, worldType));
            } catch (JsonSyntaxException e) {
                Terrarium.LOGGER.error("Failed to construct generator", e);
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        World world = event.world;
        if (ServerEventHandler.shouldHandle(world) && world instanceof WorldServer) {
            long time = System.currentTimeMillis();
            if (time - lastRegionTrackTime > REGION_TRACK_INTERVAL) {
                TerrariumWorldData worldData = ((TerrariumWorldType) world.getWorldType()).getWorldData(world);
                if (worldData != null) {
                    WorldServer worldServer = (WorldServer) world;
                    worldData.getRegionHandler().trackRegions(worldServer, worldServer.getPlayerChunkMap());
                }
                lastRegionTrackTime = time;
            }
        }
    }

    private static boolean shouldHandle(World world) {
        return world.provider.getDimensionType() == DimensionType.OVERWORLD && world.getWorldType() instanceof TerrariumWorldType;
    }
}
