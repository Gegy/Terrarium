package net.gegy1000.earth.server.world.biome;

import net.gegy1000.earth.server.world.Climate;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.soil.SoilSelector;
import net.gegy1000.earth.server.world.ecology.vegetation.Trees;
import net.gegy1000.earth.server.world.geography.Landform;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

public final class BiomeClassifier {
    public static Biome classify(GrowthPredictors predictors) {
        if (predictors.elevation == -Float.MAX_VALUE) {
            return Biomes.VOID;
        }

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

        if (predictors.isFrozen()) return classifyFrozen(predictors);
        if (predictors.isFlooded()) return classifyFlooded(predictors);

        float annualRainfall = predictors.annualRainfall;
        float meanTemperature = predictors.meanTemperature;

        if (Climate.isDesert(annualRainfall) && SoilSelector.isDesertLike(predictors)) {
            return Biomes.DESERT;
        }

        if (!predictors.isBarren()) {
            if (Climate.isTropicalRainforest(annualRainfall, meanTemperature)) {
                return predictors.isForested() ? Biomes.JUNGLE : Biomes.JUNGLE_EDGE;
            }

            if (Climate.isTaiga(annualRainfall, meanTemperature) && predictors.isForested()) {
                return Biomes.TAIGA;
            }
        }

        // non-specific selection
        if (Climate.isCold(meanTemperature)) {
            return Biomes.TAIGA;
        } else if (Climate.isDry(annualRainfall)) {
            return Biomes.SAVANNA;
        }

        return predictors.isForested() ? classifyForest(predictors) : Biomes.PLAINS;
    }

    private static Biome classifyFrozen(GrowthPredictors predictors) {
        return predictors.isForested() ? Biomes.COLD_TAIGA : Biomes.ICE_PLAINS;
    }

    private static Biome classifyFlooded(GrowthPredictors predictors) {
        if (predictors.cover == Cover.SALINE_FLOODED_FOREST) {
            return Biomes.SWAMPLAND;
        } else {
            return predictors.isForested() ? Biomes.JUNGLE : Biomes.JUNGLE_EDGE;
        }
    }

    private static Biome classifyForest(GrowthPredictors predictors) {
        double oak = Trees.Indicators.OAK.evaluate(predictors);
        double birch = Trees.Indicators.BIRCH.evaluate(predictors);

        return oak > birch ? Biomes.FOREST : Biomes.BIRCH_FOREST;
    }

    private static Biome classifyWater(GrowthPredictors predictors) {
        if (predictors.isSea()) {
            if (predictors.elevation < -500) {
                return Biomes.DEEP_OCEAN;
            } else {
                return Biomes.OCEAN;
            }
        }

        return predictors.isFrozen() ? Biomes.FROZEN_RIVER : Biomes.RIVER;
    }
}
