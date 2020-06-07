package net.gegy1000.earth.server.world.composer;

import net.gegy1000.earth.server.event.ClassifyBiomeEvent;
import net.gegy1000.earth.server.world.EarthData;
import net.gegy1000.earth.server.world.biome.BiomeClassifier;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;

public final class EarthBiomeComposer implements BiomeComposer {
    private final Biome[] biomeBuffer = new Biome[16 * 16];

    private final BiomeClassifier.Context context = new BiomeClassifier.Context();

    @Override
    public Biome[] composeBiomes(TerrariumWorld terrarium, ColumnData data, ChunkPos columnPos) {
        return data.with(
                EarthData.COVER,
                EarthData.LANDFORM,
                EarthData.MIN_TEMPERATURE,
                EarthData.MEAN_TEMPERATURE,
                EarthData.ANNUAL_RAINFALL
        ).map(with -> {
            EnumRaster<Cover> coverRaster = with.get(EarthData.COVER);
            EnumRaster<Landform> landformRaster = with.get(EarthData.LANDFORM);
            FloatRaster minTemperatureRaster = with.get(EarthData.MIN_TEMPERATURE);
            FloatRaster meanTemperatureRaster = with.get(EarthData.MEAN_TEMPERATURE);
            ShortRaster rainfallRaster = with.get(EarthData.ANNUAL_RAINFALL);

            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    this.context.annualRainfall = rainfallRaster.get(x, z);
                    this.context.minTemperature = minTemperatureRaster.get(x, z);
                    this.context.meanTemperature = meanTemperatureRaster.get(x, z);
                    this.context.cover = coverRaster.get(x, z);
                    this.context.landform = landformRaster.get(x, z);

                    this.biomeBuffer[x + z * 16] = this.classify(terrarium);
                }
            }

            return this.biomeBuffer;
        }).orElseGet(() -> ArrayUtils.fill(this.biomeBuffer, Biomes.DEFAULT));
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
