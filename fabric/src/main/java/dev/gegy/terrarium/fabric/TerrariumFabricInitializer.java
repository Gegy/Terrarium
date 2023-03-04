package dev.gegy.terrarium.fabric;

import dev.gegy.terrarium.Terrarium;
import net.fabricmc.api.ModInitializer;

public class TerrariumFabricInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        Terrarium.bootstrap();
    }
}
