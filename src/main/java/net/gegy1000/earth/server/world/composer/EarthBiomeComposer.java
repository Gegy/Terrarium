package net.gegy1000.earth.server.world.composer;

import net.gegy1000.earth.server.event.ClassifyBiomeEvent;
import net.gegy1000.earth.server.world.EarthData;
import net.gegy1000.earth.server.world.biome.BiomeClassifier;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.soil.SoilSuborder;
import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;

public final class EarthBiomeComposer implements BiomeComposer {
    private final Biome[] biomeBuffer = new Biome[16 * 16];

    private final EnumRaster.Sampler<Cover> coverSampler = EnumRaster.sampler(EarthData.COVER, Cover.NO);
    private final EnumRaster.Sampler<Landform> landformSampler = EnumRaster.sampler(EarthData.LANDFORM, Landform.LAND);
    private final EnumRaster.Sampler<SoilSuborder> soilSampler = EnumRaster.sampler(EarthData.SOIL_SUBORDER, SoilSuborder.NO);
    private final UByteRaster.Sampler slopeSampler = UByteRaster.sampler(EarthData.SLOPE);

    private final GrowthPredictors.Sampler predictorSampler = GrowthPredictors.sampler();

    private final BiomeClassifier.Context context = new BiomeClassifier.Context();

    @Override
    public Biome[] composeBiomes(TerrariumWorld terrarium, ColumnData data, ChunkPos columnPos) {
        int minX = columnPos.getXStart();
        int minZ = columnPos.getZStart();

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int x = localX + minX;
                int z = localZ + minZ;

                this.predictorSampler.sampleTo(data, x, z, this.context.predictors);
                this.context.cover = this.coverSampler.sample(data, x, z);
                this.context.landform = this.landformSampler.sample(data, x, z);
                this.context.soilSuborder = this.soilSampler.sample(data, x, z);
                this.context.slope = this.slopeSampler.sample(data, x, z);

                this.biomeBuffer[localX + localZ * 16] = this.classify(terrarium);
            }
        }

        return this.biomeBuffer;
    }

    private Biome classify(TerrariumWorld terrarium) {
        ClassifyBiomeEvent event = new ClassifyBiomeEvent(terrarium, this.context);
        if (MinecraftForge.TERRAIN_GEN_BUS.post(event)) {
            Biome biome = event.getBiome();
            if (biome != null) {
                return biome;
            }
        }

        return BiomeClassifier.classify(this.context);
    }
}
