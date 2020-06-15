package net.gegy1000.earth.server.world.ecology.maxent;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.ecology.GrowthIndicator;
import net.gegy1000.earth.server.world.ecology.maxent.feature.OutputFeature;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Optional;

public final class MaxentGrowthIndicator {
    private MaxentGrowthIndicator() {
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
        float linearPredictorNormalizer = lambdasFile.getFieldOr("linearPredictorNormalizer", 0.0F);
        float densityNormalizer = lambdasFile.getFieldOr("densityNormalizer", 1.0F);
        float entropy = lambdasFile.getFieldOr("entropy", 0.0F);

        OutputFeature feature = new OutputFeature(
                lambdasFile.getFeatures(),
                linearPredictorNormalizer,
                densityNormalizer,
                entropy,
                output
        );

        return MaxentCompiler.compileFeature(feature);
    }
}
