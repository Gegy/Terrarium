package net.gegy1000.terrarium.server;

import net.gegy1000.cubicglue.api.CubicWorldType;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.preview.PreviewDummyWorld;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumExternalCapProvider;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.chunk.PlayerChunkMapHooks;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Collection;

@Mod.EventBusSubscriber(modid = Terrarium.MODID)
public class ServerEventHandler {
    private static final long REGION_TRACK_INTERVAL = 1000;
    private static long lastRegionTrackTime;

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld();
        if (!world.isRemote && ServerEventHandler.shouldHandle(world)) {
            TerrariumWorldData worldData = TerrariumWorldData.get(world);
            if (worldData != null) {
                Coordinate spawnPosition = worldData.getSpawnPosition();
                if (spawnPosition != null) {
                    world.setSpawnPoint(spawnPosition.toBlockPos());
                }
            }
            if (world instanceof WorldServer) {
                PlayerChunkMapHooks.hookWorldMap((WorldServer) world);
            }
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        World world = event.getWorld();
        if (!world.isRemote && ServerEventHandler.shouldHandle(world)) {
            TerrariumWorldData worldData = TerrariumWorldData.get(world);
            if (worldData != null) {
                worldData.getRegionHandler().close();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        World world = event.getObject();

        if (ServerEventHandler.shouldHandle(world)) {
            TerrariumWorldType worldType = (TerrariumWorldType) CubicWorldType.unwrap(world.getWorldType());
            TerrariumExternalCapProvider external = new TerrariumExternalCapProvider.Implementation();

            if (!world.isRemote || world instanceof PreviewDummyWorld) {
                TerrariumWorldData worldData = new TerrariumWorldData.Implementation(world, worldType);

                Collection<ICapabilityProvider> capabilities = worldType.createCapabilities(world, worldData.getSettings());
                for (ICapabilityProvider provider : capabilities) {
                    external.addExternal(provider);
                }

                event.addCapability(TerrariumCapabilities.WORLD_DATA_ID, worldData);
            }

            event.addCapability(TerrariumCapabilities.EXTERNAL_DATA_ID, external);
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        World world = event.world;
        if (ServerEventHandler.shouldHandle(world) && world instanceof WorldServer) {
            long time = System.currentTimeMillis();
            if (time - lastRegionTrackTime > REGION_TRACK_INTERVAL) {
                TerrariumWorldData worldData = TerrariumWorldData.get(world);
                if (worldData != null) {
                    WorldServer worldServer = (WorldServer) world;
                    worldData.getRegionHandler().trackRegions(worldServer, worldServer.getPlayerChunkMap());
                }
                lastRegionTrackTime = time;
            }
        }
    }

    private static boolean shouldHandle(World world) {
        CubicWorldType worldType = CubicWorldType.unwrap(world.getWorldType());
        return world.provider.getDimensionType() == DimensionType.OVERWORLD && worldType instanceof TerrariumWorldType;
    }
}
