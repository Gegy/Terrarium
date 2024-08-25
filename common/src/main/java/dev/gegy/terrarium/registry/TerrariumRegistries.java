package dev.gegy.terrarium.registry;

import com.mojang.serialization.Codec;
import dev.gegy.terrarium.Terrarium;
import dev.gegy.terrarium.backend.earth.GeoParameters;
import dev.gegy.terrarium.backend.expr.classifier.ClassifierNode;
import dev.gegy.terrarium.backend.expr.predictor.Predictor;
import dev.gegy.terrarium.backend.expr.predictor.PredictorNode;
import dev.gegy.terrarium.backend.util.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class TerrariumRegistries {
    public static final ResourceKey<Registry<Predictor<GeoParameters>>> BUILTIN_PREDICTOR = createKey("builtin_predictor");

    public static final ResourceKey<Registry<PredictorNode<GeoParameters>>> PREDICTOR = createKey("predictor");
    public static final ResourceKey<Registry<ClassifierNode<GeoParameters, Holder<Biome>>>> BIOME_CLASSIFIER = createKey("classifier/biome");

    public static final Codec<Holder<ClassifierNode<GeoParameters, Holder<Biome>>>> BIOME_CLASSIFIER_CODEC = RegistryFileCodec.create(BIOME_CLASSIFIER, Util.lazyCodec(Terrarium::biomeClassifierCodec));

    private static <T> ResourceKey<Registry<T>> createKey(final String name) {
        return ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Terrarium.ID, name));
    }
}
