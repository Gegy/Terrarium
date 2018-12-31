package net.gegy1000.terrarium.server;

import net.fabricmc.fabric.events.TickEvent;
import net.gegy1000.terrarium.api.CustomLevelGenerator;
import net.gegy1000.terrarium.server.event.WorldEvent;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorConfig;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorType;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class ServerEventHandler {
    private static final long REGION_TRACK_INTERVAL = 1000;
    private static long lastRegionTrackTime;

    public static void register() {
        WorldEvent.LOAD.register(ServerEventHandler::onWorldLoad);
        WorldEvent.UNLOAD.register(ServerEventHandler::onWorldUnload);
        TickEvent.WORLD.register(ServerEventHandler::onWorldTick);
    }

    private static void onWorldLoad(World world) {
        if (!world.isClient && ServerEventHandler.shouldHandle(world)) {
            TerrariumGeneratorConfig config = TerrariumGeneratorConfig.get(world);
            if (config != null) {
                Coordinate spawnPosition = config.getSpawnPosition();
                if (spawnPosition != null) {
                    world.setSpawnPos(spawnPosition.toBlockPos());
                }
            }
        }
    }

    private static void onWorldUnload(World world) {
        if (!world.isClient && ServerEventHandler.shouldHandle(world)) {
            TerrariumGeneratorConfig config = TerrariumGeneratorConfig.get(world);
            if (config != null) {
                config.getRegionHandler().close();
            }
        }
    }

    private static void onWorldTick(World world) {
        if (ServerEventHandler.shouldHandle(world) && world instanceof ServerWorld) {
            long time = System.currentTimeMillis();
            if (time - lastRegionTrackTime > REGION_TRACK_INTERVAL) {
                TerrariumGeneratorConfig config = TerrariumGeneratorConfig.get(world);
                if (config != null) {
                    ServerWorld serverWorld = (ServerWorld) world;
                    config.getRegionHandler().trackRegions(serverWorld, serverWorld.getChunkManager());
                }
                lastRegionTrackTime = time;
            }
        }
    }

    private static boolean shouldHandle(World world) {
        CustomLevelGenerator customGenerator = CustomLevelGenerator.unwrap(world.getGeneratorType());
        return world.dimension.getType() == DimensionType.OVERWORLD && customGenerator instanceof TerrariumGeneratorType;
    }
}
