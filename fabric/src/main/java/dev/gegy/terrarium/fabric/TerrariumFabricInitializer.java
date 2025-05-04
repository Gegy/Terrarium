package dev.gegy.terrarium.fabric;

import com.mojang.serialization.Codec;
import dev.gegy.terrarium.Terrarium;
import dev.gegy.terrarium.backend.earth.GeoParameters;
import dev.gegy.terrarium.backend.expr.classifier.ClassifierNode;
import dev.gegy.terrarium.backend.expr.predictor.Predictor;
import dev.gegy.terrarium.backend.expr.predictor.PredictorNode;
import dev.gegy.terrarium.registry.TerrariumRegistries;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.world.level.biome.Biome;

import java.nio.file.Path;

public class TerrariumFabricInitializer implements ModInitializer {
    private static final MappedRegistry<Predictor<GeoParameters>> BUILTIN_PREDICTORS = FabricRegistryBuilder.createSimple(TerrariumRegistries.BUILTIN_PREDICTOR).buildAndRegister();

    @Override
    public void onInitialize() {
        final FabricLoader loader = FabricLoader.getInstance();
        final Path gameDirectory = loader.getGameDir();
        final Path terrariumDirectory = gameDirectory.resolve("mods").resolve("terrarium");

        final Terrarium.PlatformBootstrap platform = new Terrarium.PlatformBootstrap() {
            @Override
            public void initializeRegistries(final Codec<PredictorNode<GeoParameters>> predictorCodec, final Codec<ClassifierNode<GeoParameters, Holder<Biome>>> biomeClassifierCodec) {
                DynamicRegistries.register(TerrariumRegistries.PREDICTOR, predictorCodec);
                DynamicRegistries.register(TerrariumRegistries.BIOME_CLASSIFIER, biomeClassifierCodec);
            }

            @Override
            public boolean isModLoaded(final String id) {
                return loader.isModLoaded(id);
            }
        };

        Terrarium.bootstrap(terrariumDirectory, BUILTIN_PREDICTORS, loader.isDevelopmentEnvironment(), platform);

        CommandRegistrationCallback.EVENT.register((dispatcher, context, environment) -> Terrarium.registerCommands(dispatcher, context));
    }
}
