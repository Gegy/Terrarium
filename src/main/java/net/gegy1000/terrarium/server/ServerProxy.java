package net.gegy1000.terrarium.server;

import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumPresetRegistry;
import net.gegy1000.terrarium.server.world.pipeline.source.CachedRemoteSource;

public class ServerProxy {
    public void onPreInit() {
        if (!CachedRemoteSource.GLOBAL_CACHE_ROOT.exists()) {
            CachedRemoteSource.GLOBAL_CACHE_ROOT.mkdirs();
        }

        TerrariumCapabilities.onPreInit();
    }

    public void onInit() {
        TerrariumPresetRegistry.onInit();
    }

    public void onPostInit() {
    }
}
