package net.gegy1000.earth.server.world.composer;

import net.gegy1000.earth.server.event.ClassifyBiomeEvent;
import net.gegy1000.earth.server.world.biome.BiomeClassifier;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;

public final class EarthBiomeComposer implements BiomeComposer {
    private final Biome[] biomeBuffer = new Biome[16 * 16];

    private final GrowthPredictors.Sampler predictorSampler = GrowthPredictors.sampler();

    private final GrowthPredictors predictors = new GrowthPredictors();

    @Override
    public Biome[] composeBiomes(TerrariumWorld terrarium, ColumnData data, ChunkPos columnPos) {
        int minX = columnPos.getXStart();
        int minZ = columnPos.getZStart();

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int x = localX + minX;
                int z = localZ + minZ;

                this.predictorSampler.sampleTo(data, x, z, this.predictors);
                this.biomeBuffer[localX + localZ * 16] = this.classify(terrarium);
            }
        }

        return this.biomeBuffer;
    }

    private Biome classify(TerrariumWorld terrarium) {
        Biome biome = BiomeClassifier.classify(this.predictors);

        ClassifyBiomeEvent event = new ClassifyBiomeEvent(terrarium, this.predictors, biome);
        MinecraftForge.TERRAIN_GEN_BUS.post(event);

        return event.getBiome();
    }
}
