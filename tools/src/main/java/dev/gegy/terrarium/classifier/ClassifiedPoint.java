package dev.gegy.terrarium.classifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gegy.terrarium.Biome;
import dev.gegy.terrarium.backend.earth.GeoParameters;

import java.util.List;

public record ClassifiedPoint(
        double latitude,
        double longitude,
        GeoParameters parameters,
        Biome biome
) {
    public static final Codec<ClassifiedPoint> CODEC = RecordCodecBuilder.create(i -> i.group(
    		Codec.DOUBLE.fieldOf("latitude").forGetter(ClassifiedPoint::latitude),
            Codec.DOUBLE.fieldOf("longitude").forGetter(ClassifiedPoint::longitude),
            GeoParameters.CODEC.fieldOf("parameters").forGetter(ClassifiedPoint::parameters),
            Biome.CODEC.fieldOf("biome").forGetter(ClassifiedPoint::biome)
    ).apply(i, ClassifiedPoint::new));

    public static final Codec<List<ClassifiedPoint>> LIST_CODEC = ClassifiedPoint.CODEC.listOf();

    public ClassifierTrainer.Sample<GeoParameters, Biome> toSample() {
        return new ClassifierTrainer.Sample<>(parameters, biome());
    }
}
