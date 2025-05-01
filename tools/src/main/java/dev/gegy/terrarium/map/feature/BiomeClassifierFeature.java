package dev.gegy.terrarium.map.feature;

import dev.gegy.terrarium.Biome;
import dev.gegy.terrarium.backend.earth.GeoParameters;
import dev.gegy.terrarium.backend.expr.classifier.Classifier;
import dev.gegy.terrarium.backend.expr.classifier.ClassifierNode;

public record BiomeClassifierFeature(Classifier<GeoParameters, Biome> classifier) implements DerivedFeature {
    public BiomeClassifierFeature(final ClassifierNode<GeoParameters, Biome> classifier) {
        this(ClassifierNode.compile(classifier));
    }

    @Override
    public int getColor(final GeoParameters parameters) {
        if (parameters.elevation() < 0.0f) {
            return Biome.OCEAN.color();
        }
        return classifier.evaluate(parameters).color();
    }
}
