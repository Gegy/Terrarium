package net.gegy1000.earth.server.world.composer;

import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.world.biome.BiomeClassifier;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.data.DataSample;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

import java.util.Arrays;

public final class EarthBiomeComposer implements BiomeComposer {
    private final GrowthPredictors.Sampler predictorSampler = GrowthPredictors.sampler();

    private final GrowthPredictors predictors = new GrowthPredictors();

    @Override
    public void composeBiomes(Biome[] buffer, TerrariumWorld terrarium, DataSample data, DataView view) {
        int width = view.width();
        int height = view.height();

        EarthWorld earth = EarthWorld.get(terrarium.getWorld());
        if (earth == null) {
            Arrays.fill(buffer, Biomes.DEFAULT);
            return;
        }

        BiomeClassifier classifier = earth.getBiomeClassifier();

        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                this.predictorSampler.sampleTo(data, x, z, this.predictors);
                buffer[x + z * width] = classifier.classify(this.predictors);
            }
        }
    }
}
