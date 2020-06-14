package net.gegy1000.earth.server.world.composer;

import net.gegy1000.earth.server.event.ClassifyBiomeEvent;
import net.gegy1000.earth.server.world.biome.BiomeClassifier;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;

public final class EarthBiomeComposer implements BiomeComposer {
    private final GrowthPredictors.Sampler predictorSampler = GrowthPredictors.sampler();

    private final GrowthPredictors predictors = new GrowthPredictors();

    @Override
    public void composeBiomes(Biome[] buffer, TerrariumWorld terrarium, ColumnData data, DataView view) {
        int width = view.getWidth();
        int height = view.getHeight();

        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                this.predictorSampler.sampleTo(data, x, z, this.predictors);
                buffer[x + z * width] = this.classify(terrarium);
            }
        }
    }

    private Biome classify(TerrariumWorld terrarium) {
        Biome biome = BiomeClassifier.classify(this.predictors);

        ClassifyBiomeEvent event = new ClassifyBiomeEvent(terrarium, this.predictors, biome);
        MinecraftForge.TERRAIN_GEN_BUS.post(event);

        return event.getBiome();
    }
}
