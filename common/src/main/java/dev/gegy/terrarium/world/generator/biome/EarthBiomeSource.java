package dev.gegy.terrarium.world.generator.biome;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gegy.terrarium.backend.GeoChunk;
import dev.gegy.terrarium.backend.earth.EarthAttachments;
import dev.gegy.terrarium.backend.earth.GeoParameters;
import dev.gegy.terrarium.backend.expr.classifier.Classifier;
import dev.gegy.terrarium.backend.expr.classifier.ClassifierNode;
import dev.gegy.terrarium.registry.TerrariumRegistries;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class EarthBiomeSource extends GeoBiomeSource {
    public static final MapCodec<EarthBiomeSource> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            TerrariumRegistries.BIOME_CLASSIFIER_CODEC.fieldOf("classifier").forGetter(s -> s.biomeClassifierNode),
            Biome.CODEC.fieldOf("fallback").forGetter(s -> s.fallbackBiome)
    ).apply(i, EarthBiomeSource::new));

    private final Holder<ClassifierNode<GeoParameters, Holder<Biome>>> biomeClassifierNode;
    private final Supplier<Classifier<GeoParameters, Holder<Biome>>> biomeClassifier;
    private final Holder<Biome> fallbackBiome;

    public EarthBiomeSource(final Holder<ClassifierNode<GeoParameters, Holder<Biome>>> biomeClassifierNode, final Holder<Biome> fallbackBiome) {
        this.biomeClassifierNode = biomeClassifierNode;
        biomeClassifier = Suppliers.memoize(() -> ClassifierNode.compile(biomeClassifierNode.value()));
        this.fallbackBiome = fallbackBiome;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        final Set<Holder<Biome>> biomes = new ReferenceOpenHashSet<>();
        biomes.add(fallbackBiome);
        biomeClassifierNode.value().forEachPossibleValue(biomes::add);
        return biomes.stream();
    }

    @Override
    public FlatChunkResolver chunkResolver(final GeoChunk geoChunk) {
        final EarthAttachments attachments = EarthAttachments.from(geoChunk).orElse(null);
        if (attachments == null) {
            return (x, z) -> fallbackBiome;
        }
        final GeoParameters parameters = new GeoParameters();
        final Classifier<GeoParameters, Holder<Biome>> biomeClassifier = this.biomeClassifier.get();
        return (x, z) -> biomeClassifier.evaluate(parameters.set(attachments, x, z));
    }
}
