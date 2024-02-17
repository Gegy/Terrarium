package dev.gegy.terrarium.registry;

import dev.gegy.terrarium.Terrarium;
import dev.gegy.terrarium.backend.earth.GeoParameters;
import dev.gegy.terrarium.backend.expr.classifier.ClassifierNode;
import dev.gegy.terrarium.backend.expr.classifier.Classifiers;
import dev.gegy.terrarium.backend.expr.predictor.PredictorNode;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import static dev.gegy.terrarium.backend.expr.classifier.Classifiers.threshold;
import static dev.gegy.terrarium.backend.expr.predictor.Predictors.constant;
import static dev.gegy.terrarium.backend.expr.predictor.Predictors.opaque;

public class TerrariumBiomeClassifiers {
    public static final ResourceKey<ClassifierNode<GeoParameters, Holder<Biome>>> EARTH = createKey("earth");
    public static final ResourceKey<ClassifierNode<GeoParameters, Holder<Biome>>> EARTH_LAND = createKey("earth_land");
    public static final ResourceKey<ClassifierNode<GeoParameters, Holder<Biome>>> EARTH_OCEAN = createKey("earth_ocean");

    public static void bootstrap(final BootstapContext<ClassifierNode<GeoParameters, Holder<Biome>>> context) {
        final HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        context.register(EARTH, threshold(
                elevation(), constant(0.0f),
                register(context, EARTH_LAND,
                        leaf(biomes, Biomes.PLAINS)
                ),
                register(context, EARTH_OCEAN,
                        leaf(biomes, Biomes.OCEAN)
                )
        ));
    }

    private static PredictorNode<GeoParameters> elevation() {
        return opaque(GeoParameters.ELEVATION);
    }

    private static ClassifierNode<GeoParameters, Holder<Biome>> leaf(final HolderGetter<Biome> biomes, final ResourceKey<Biome> key) {
        return Classifiers.leaf(biomes.getOrThrow(key));
    }

    private static ClassifierNode<GeoParameters, Holder<Biome>> register(final BootstapContext<ClassifierNode<GeoParameters, Holder<Biome>>> context, final ResourceKey<ClassifierNode<GeoParameters, Holder<Biome>>> key, final ClassifierNode<GeoParameters, Holder<Biome>> node) {
        return new HolderClassifierNode<>(context.register(key, node));
    }

    private static ResourceKey<ClassifierNode<GeoParameters, Holder<Biome>>> createKey(final String name) {
        return ResourceKey.create(TerrariumRegistries.BIOME_CLASSIFIER, new ResourceLocation(Terrarium.ID, name));
    }
}
