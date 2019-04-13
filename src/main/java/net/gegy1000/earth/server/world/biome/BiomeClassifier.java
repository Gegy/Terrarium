package net.gegy1000.earth.server.world.biome;

import net.minecraftforge.common.BiomeDictionary;

public final class BiomeClassifier {
    public static void classifyRainfall(BiomeClassification classification, short rainfall) {
        if (rainfall < 250) {
            classification.include(BiomeDictionary.Type.DRY);
        } else if (rainfall > 1500) {
            classification.include(BiomeDictionary.Type.WET);
        } else {
            classification.exclude(BiomeDictionary.Type.WET);
            classification.exclude(BiomeDictionary.Type.DRY);
        }
    }

    public static void classifyTemperature(BiomeClassification classification, float temperature) {
        if (temperature < 12.0F) {
            if (temperature < 0.0F) {
                classification.include(BiomeDictionary.Type.SNOWY);
            }
            classification.include(BiomeDictionary.Type.COLD);
        } else if (temperature > 22.0F) {
            classification.include(BiomeDictionary.Type.HOT);
        } else {
            classification.exclude(BiomeDictionary.Type.COLD);
            classification.exclude(BiomeDictionary.Type.HOT);
        }
    }
}
