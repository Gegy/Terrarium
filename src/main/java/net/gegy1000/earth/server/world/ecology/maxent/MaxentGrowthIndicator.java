package net.gegy1000.earth.server.world.ecology.maxent;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.ecology.GrowthIndicator;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.maxent.feature.SumFeature;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Optional;

public final class MaxentGrowthIndicator implements GrowthIndicator {
    private final GrowthIndicator sum;
    private final double linearPredictorNormalizer;
    private final double densityNormalizer;
    private final double entropyExp;
    private final MaxentOutput output;

    private MaxentGrowthIndicator(
            GrowthIndicator sum,
            float linearPredictorNormalizer, float densityNormalizer, float entropy,
            MaxentOutput output
    ) {
        this.sum = sum;
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
        SumFeature feature = new SumFeature(lambdasFile.getFeatures());
        GrowthIndicator sum = MaxentCompiler.compileFeature(feature);

        float linearPredictorNormalizer = lambdasFile.getFieldOr("linearPredictorNormalizer", 0.0F);
        float densityNormalizer = lambdasFile.getFieldOr("densityNormalizer", 1.0F);
        float entropy = lambdasFile.getFieldOr("entropy", 0.0F);
        return new MaxentGrowthIndicator(sum, linearPredictorNormalizer, densityNormalizer, entropy, output);
    }

    @Override
    public float evaluate(GrowthPredictors predictors) {
        float sum = this.sum.evaluate(predictors);

        double raw = (Math.exp(sum - this.linearPredictorNormalizer) / this.densityNormalizer);
        switch (this.output) {
            case LOGISTIC:
                return (float) (1.0 - 1.0 / (raw * this.entropyExp + 1.0));
            case CLOGLOG:
                return (float) (1.0 - Math.exp(raw * -this.entropyExp));
            case RAW:
            default:
                return (float) raw;
        }
    }
}
