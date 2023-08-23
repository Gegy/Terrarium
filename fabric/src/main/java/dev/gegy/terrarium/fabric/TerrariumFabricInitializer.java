package dev.gegy.terrarium.fabric;

import dev.gegy.terrarium.Terrarium;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class TerrariumFabricInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        final Path gameDir = FabricLoader.getInstance().getGameDir();
        Terrarium.bootstrap(gameDir.resolve("mods").resolve("terrarium"));
    }
}
