package net.gegy1000.earth.server.world.compatibility.hooks;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public final class DimensionManagerHooks {
    // to construct a WorldServer, we need to make sure Forge doesn't update the world map
    public static void restoreWorldMapping(WorldServer world) {
        MinecraftServer server = world.getMinecraftServer();
        int dimensionId = world.provider.getDimension();

        DimensionManager.setWorld(dimensionId, null, server);
        DimensionManager.setWorld(dimensionId, world, server);
    }
}
