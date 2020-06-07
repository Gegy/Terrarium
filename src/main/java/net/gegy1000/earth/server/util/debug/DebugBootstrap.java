package net.gegy1000.earth.server.util.debug;

import net.gegy1000.earth.server.world.cover.CoverMarkers;
import net.minecraft.init.Bootstrap;

public final class DebugBootstrap {
    public static void run() {
        Bootstrap.register();
        CoverMarkers.register();
    }
}
