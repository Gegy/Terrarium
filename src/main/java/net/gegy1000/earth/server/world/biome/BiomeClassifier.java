package net.gegy1000.earth.server.world.biome;

import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

public interface BiomeClassifier {
    BiomeClassifier DEFAULT = predictors -> Biomes.DEFAULT;

    Biome classify(GrowthPredictors predictors);
}
