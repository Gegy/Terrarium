package net.gegy1000.terrarium.server;

import net.gegy1000.gengen.api.GenericWorldType;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumAuxCaps;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.util.Profiler;
import net.gegy1000.terrarium.server.util.ThreadedProfiler;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.source.DataSourceReader;
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

@Mod.EventBusSubscriber(modid = Terrarium.ID)
public class ServerEventHandler {
    private static final long DATA_TRACK_INTERVAL = 2000;
    private static long lastDataTrackTime;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSetWorldSpawn(WorldEvent.CreateSpawnPosition event) {
        World world = event.getWorld();
        if (!world.isRemote && ServerEventHandler.shouldHandle(world)) {
            TerrariumWorld terrarium = TerrariumWorld.get(world);
            if (terrarium != null) {
                Coordinate spawnPosition = terrarium.getSpawnPosition();
                if (spawnPosition != null) {
                    world.setSpawnPoint(spawnPosition.toBlockPos());
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        World world = event.getWorld();
        if (!world.isRemote && ServerEventHandler.shouldHandle(world)) {
            TerrariumWorld worldData = TerrariumWorld.get(world);
            if (worldData != null) {
                worldData.getDataCache().close();
                DataSourceReader.INSTANCE.clear();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        World world = event.getObject();

        if (ServerEventHandler.shouldHandle(world)) {
            TerrariumWorldType worldType = GenericWorldType.unwrapAs(world.getWorldType(), TerrariumWorldType.class);
            if (worldType == null) return;

            TerrariumAuxCaps aux = new TerrariumAuxCaps.Implementation();

            if (!world.isRemote && world instanceof WorldServer) {
                TerrariumWorld terrarium = new TerrariumWorld.Impl((WorldServer) world, worldType);

                Collection<ICapabilityProvider> capabilities = worldType.createCapabilities(world, terrarium.getSettings());
                for (ICapabilityProvider provider : capabilities) {
                    aux.addAux(provider);
                }

                event.addCapability(TerrariumCapabilities.WORLD_DATA_ID, terrarium);
            }

            event.addCapability(TerrariumCapabilities.AUX_DATA_ID, aux);
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        World world = event.world;
        if (event.phase == TickEvent.Phase.START && ServerEventHandler.shouldHandle(world) && world instanceof WorldServer) {
            TerrariumWorld terrarium = TerrariumWorld.get(world);
            if (terrarium == null) return;

            Profiler profiler = ThreadedProfiler.get();
            try (Profiler.Handle tick = profiler.push("tick")) {
                ColumnDataCache dataCache = terrarium.getDataCache();

                try (Profiler.Handle advance = profiler.push("advance")) {
                    // advance loading for up to 2ms
                    long allocation = 2 * 1000000;
                    dataCache.advanceUntil(System.nanoTime() + allocation);
                }

                long time = System.currentTimeMillis();
                if (time - lastDataTrackTime > DATA_TRACK_INTERVAL) {
                    try (Profiler.Handle track = profiler.push("track_columns")) {
                        dataCache.dropColumns();
                        dataCache.trackColumns();
                        lastDataTrackTime = time;
                    }
                }
            }
        }
    }

    private static boolean shouldHandle(World world) {
        GenericWorldType worldType = GenericWorldType.unwrap(world.getWorldType());
        return world.provider.getDimensionType() == DimensionType.OVERWORLD && worldType instanceof TerrariumWorldType;
    }
}
