package dev.gegy.terrarium.registry;

import dev.gegy.terrarium.Terrarium;
import dev.gegy.terrarium.backend.earth.GeoParameters;
import dev.gegy.terrarium.backend.earth.cover.Cover;
import dev.gegy.terrarium.backend.expr.classifier.ClassifierNode;
import dev.gegy.terrarium.backend.expr.classifier.Classifiers;
import dev.gegy.terrarium.backend.expr.predictor.PredictorNode;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
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

    public static void bootstrap(final BootstrapContext<ClassifierNode<GeoParameters, Holder<Biome>>> context) {
        final HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        context.register(EARTH, threshold(
                elevation(), constant(0.0f),
                linkData(EARTH_LAND),
                register(context, EARTH_OCEAN, buildOceanClassifier(biomes))
        ));
    }

    private static ClassifierNode<GeoParameters, Holder<Biome>> buildOceanClassifier(final HolderGetter<Biome> biomes) {
        return threshold(
                meanTemperature(), constant(5.0f),
                threshold(
                        meanTemperature(), constant(18.0f),
                        threshold(
                                meanTemperature(), constant(22.0f),
                                oceanChoice(biomes, Biomes.WARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN),
                                oceanChoice(biomes, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN)
                        ),
                        oceanChoice(biomes, Biomes.OCEAN, Biomes.DEEP_OCEAN)
                ),
                threshold(
                        minTemperature(), constant(-14.0f),
                        oceanChoice(biomes, Biomes.COLD_OCEAN, Biomes.DEEP_COLD_OCEAN),
                        oceanChoice(biomes, Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN)
                )
        );
    }

    private static ClassifierNode<GeoParameters, Holder<Biome>> oceanChoice(final HolderGetter<Biome> biomes, final ResourceKey<Biome> ocean, final ResourceKey<Biome> deepOcean) {
        return threshold(
                elevation(), constant(-1000.0f),
                leaf(biomes, ocean),
                leaf(biomes, deepOcean)
        );
    }

    private static PredictorNode<GeoParameters> elevation() {
        return opaque(GeoParameters.ELEVATION);
    }

    private static PredictorNode<GeoParameters> meanTemperature() {
        return opaque(GeoParameters.MEAN_TEMPERATURE);
    }

    private static PredictorNode<GeoParameters> minTemperature() {
        return opaque(GeoParameters.MIN_TEMPERATURE);
    }

    private static PredictorNode<GeoParameters> is(final Cover cover) {
        return opaque(GeoParameters.IS_COVER.get(cover));
    }

    private static ClassifierNode<GeoParameters, Holder<Biome>> leaf(final HolderGetter<Biome> biomes, final ResourceKey<Biome> key) {
        return Classifiers.leaf(biomes.getOrThrow(key));
    }

    private static ClassifierNode<GeoParameters, Holder<Biome>> register(final BootstrapContext<ClassifierNode<GeoParameters, Holder<Biome>>> context, final ResourceKey<ClassifierNode<GeoParameters, Holder<Biome>>> key, final ClassifierNode<GeoParameters, Holder<Biome>> node) {
        return new HolderClassifierNode<>(context.register(key, node));
    }

    private static ClassifierNode<GeoParameters, Holder<Biome>> linkData(final ResourceKey<ClassifierNode<GeoParameters, Holder<Biome>>> key) {
        // Big hack - we don't want to let generators know about this Holder, as it is otherwise required to be registered
        return new HolderClassifierNode<>(Holder.Reference.createStandAlone(new UniversalOwner<>(), key));
    }

    private static ResourceKey<ClassifierNode<GeoParameters, Holder<Biome>>> createKey(final String name) {
        return ResourceKey.create(TerrariumRegistries.BIOME_CLASSIFIER, ResourceLocation.fromNamespaceAndPath(Terrarium.ID, name));
    }

    private record UniversalOwner<T>() implements HolderOwner<T> {
        @Override
        public boolean canSerializeIn(final HolderOwner<T> owner) {
            return true;
        }
    }
}
