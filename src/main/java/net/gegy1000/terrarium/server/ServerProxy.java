package net.gegy1000.terrarium.server;

import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateStateRegistry;
import net.gegy1000.terrarium.server.world.generator.TerrariumGeneratorRegistry;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumPresetRegistry;
import net.gegy1000.terrarium.server.world.generator.customization.WidgetParseHandler;
import net.gegy1000.terrarium.server.world.json.ValueProviderRegistry;
import net.gegy1000.terrarium.server.world.pipeline.DataPipelineRegistries;
import net.gegy1000.terrarium.server.world.pipeline.composer.ComposerRegistries;
import net.gegy1000.terrarium.server.world.pipeline.source.CachedRemoteSource;
import net.gegy1000.terrarium.server.world.pipeline.source.GeocoderRegistry;
import net.minecraftforge.fml.common.ProgressManager;

public class ServerProxy {
    public void onPreInit() {
        if (!CachedRemoteSource.GLOBAL_CACHE_ROOT.exists()) {
            CachedRemoteSource.GLOBAL_CACHE_ROOT.mkdirs();
        }

        TerrariumCapabilities.onPreInit();
    }

    public void onInit() {
        ProgressManager.ProgressBar registryBar = ProgressManager.push("Loading Terrarium Registries", 4);

        registryBar.step("Preparation");
        ValueProviderRegistry.onInit();
        CoordinateStateRegistry.onInit();
        WidgetParseHandler.onInit();
        GeocoderRegistry.onInit();
        ComposerRegistries.onInit();

        registryBar.step("Data Pipeline");
        DataPipelineRegistries.onInit();

        registryBar.step("Generators");
        TerrariumGeneratorRegistry.onInit();

        registryBar.step("Presets");
        TerrariumPresetRegistry.onInit();

        ProgressManager.pop(registryBar);
    }

    public void onPostInit() {
    }
}
