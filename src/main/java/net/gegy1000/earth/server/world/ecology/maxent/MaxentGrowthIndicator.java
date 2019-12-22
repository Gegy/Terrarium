package net.gegy1000.earth.server.world.ecology.maxent;

import com.google.common.collect.ImmutableList;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.ecology.GrowthIndicator;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Optional;

public final class MaxentGrowthIndicator implements GrowthIndicator {
    private final ImmutableList<GrowthIndicator> features;
    private final double linearPredictorNormalizer;
    private final double densityNormalizer;
    private final double entropyExp;
    private final MaxentOutput output;

    private MaxentGrowthIndicator(
            ImmutableList<GrowthIndicator> features,
            double linearPredictorNormalizer, double densityNormalizer, double entropy,
            MaxentOutput output
    ) {
        this.features = features;
        this.linearPredictorNormalizer = linearPredictorNormalizer;
        this.densityNormalizer = densityNormalizer;
        this.entropyExp = Math.exp(entropy);
        this.output = output;
    }

    public static Optional<GrowthIndicator> tryParse(ResourceLocation location) {
        try {
            MaxentLambdasFile lambdasFile = MaxentLambdasFile.parse(location);
            return Optional.of(from(lambdasFile, MaxentOutput.CLOGLOG));
        } catch (IOException | MaxentParseException e) {
            TerrariumEarth.LOGGER.error("Failed to load maxent growth indicator at {}", location, e);
            return Optional.empty();
        }
    }

    public static GrowthIndicator from(MaxentLambdasFile lambdasFile, MaxentOutput output) {
        ImmutableList<GrowthIndicator> features = lambdasFile.getFeatures();
        double linearPredictorNormalizer = lambdasFile.getFieldOr("linearPredictorNormalizer", 0.0);
        double densityNormalizer = lambdasFile.getFieldOr("densityNormalizer", 1.0);
        double entropy = lambdasFile.getFieldOr("entropy", 0.0);
        return new MaxentGrowthIndicator(features, linearPredictorNormalizer, densityNormalizer, entropy, output);
    }

    @Override
    public double evaluate(GrowthPredictors predictors) {
        double sum = 0.0;
        for (GrowthIndicator feature : this.features) {
            sum += feature.evaluate(predictors);
        }

        double raw = Math.exp(sum - this.linearPredictorNormalizer) / this.densityNormalizer;
        switch (this.output) {
            case LOGISTIC:
                return 1.0 - 1.0 / (raw * this.entropyExp + 1.0);
            case CLOGLOG:
                return 1.0 - Math.exp(raw * -this.entropyExp);
            case RAW:
            default:
                return raw;
        }
    }
}
