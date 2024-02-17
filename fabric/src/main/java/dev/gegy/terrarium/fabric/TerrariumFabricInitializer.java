package dev.gegy.terrarium.fabric;

import dev.gegy.terrarium.Terrarium;
import dev.gegy.terrarium.backend.earth.GeoParameters;
import dev.gegy.terrarium.backend.expr.predictor.Predictor;
import dev.gegy.terrarium.registry.TerrariumRegistries;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.MappedRegistry;

import java.nio.file.Path;

public class TerrariumFabricInitializer implements ModInitializer {
    private static final MappedRegistry<Predictor<GeoParameters>> BUILTIN_PREDICTORS = FabricRegistryBuilder.createSimple(TerrariumRegistries.BUILTIN_PREDICTOR).buildAndRegister();

    @Override
    public void onInitialize() {
        final Path gameDir = FabricLoader.getInstance().getGameDir();
        Terrarium.bootstrap(
                gameDir.resolve("mods").resolve("terrarium"),
                BUILTIN_PREDICTORS,
                (predictorCodec, biomeClassifierCodec) -> {
                    DynamicRegistries.register(TerrariumRegistries.PREDICTOR, predictorCodec);
                    DynamicRegistries.register(TerrariumRegistries.BIOME_CLASSIFIER, biomeClassifierCodec);
                }
        );
    }
}
