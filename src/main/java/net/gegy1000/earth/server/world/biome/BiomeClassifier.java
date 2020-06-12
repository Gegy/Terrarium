package net.gegy1000.earth.server.world.biome;

import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.soil.SoilSelector;
import net.gegy1000.earth.server.world.geography.Landform;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

public final class BiomeClassifier {
    public static Biome classify(GrowthPredictors predictors) {
        if (predictors.isLand()) {
            return classifyLand(predictors);
        } else {
            return classifyWater(predictors);
        }
    }

    private static Biome classifyLand(GrowthPredictors predictors) {
        if (predictors.landform == Landform.BEACH) {
            return predictors.isFrozen() ? Biomes.COLD_BEACH : Biomes.BEACH;
        }

        if (predictors.isFrozen()) {
            return predictors.isForested() ? Biomes.COLD_TAIGA : Biomes.ICE_PLAINS;
        }
        if (predictors.isCold()) {
            return Biomes.TAIGA;
        }

        if (predictors.isWet() || predictors.isFlooded()) {
            if (predictors.cover == Cover.SALINE_FLOODED_FOREST) {
                return Biomes.SWAMPLAND;
            } else {
                return predictors.isForested() ? Biomes.JUNGLE : Biomes.JUNGLE_EDGE;
            }
        }

        if (predictors.isDry()) {
            if (predictors.isBarren() || SoilSelector.isDesertLike(predictors)) {
                return Biomes.DESERT;
            } else {
                return Biomes.SAVANNA;
            }
        }

        return predictors.isForested() ? Biomes.FOREST : Biomes.PLAINS;
    }

    private static Biome classifyWater(GrowthPredictors predictors) {
        if (predictors.isSea()) {
            return predictors.meanTemperature < -10 ? Biomes.FROZEN_OCEAN : Biomes.OCEAN;
        }

        return predictors.isFrozen() ? Biomes.FROZEN_RIVER : Biomes.RIVER;
    }
}
