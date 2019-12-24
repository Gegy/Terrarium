package net.gegy1000.earth.server.world.composer;

import net.gegy1000.earth.server.world.EarthDataKeys;
import net.gegy1000.earth.server.world.biome.BiomeClassifier;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;

public final class EarthBiomeComposer implements BiomeComposer {
    private final Biome[] biomeBuffer = new Biome[16 * 16];

    private final BiomeClassifier.Context context = new BiomeClassifier.Context();

    @Override
    public Biome[] composeBiomes(ColumnData data, ChunkPos columnPos) {
        return data.with(
                EarthDataKeys.COVER,
                EarthDataKeys.LANDFORM,
                EarthDataKeys.MIN_TEMPERATURE,
                EarthDataKeys.MEAN_TEMPERATURE,
                EarthDataKeys.ANNUAL_RAINFALL
        ).map(with -> {
            EnumRaster<Cover> coverRaster = with.get(EarthDataKeys.COVER);
            EnumRaster<Landform> landformRaster = with.get(EarthDataKeys.LANDFORM);
            FloatRaster minTemperatureRaster = with.get(EarthDataKeys.MIN_TEMPERATURE);
            FloatRaster meanTemperatureRaster = with.get(EarthDataKeys.MEAN_TEMPERATURE);
            ShortRaster rainfallRaster = with.get(EarthDataKeys.ANNUAL_RAINFALL);

            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    this.context.annualRainfall = rainfallRaster.get(x, z);
                    this.context.minTemperature = minTemperatureRaster.get(x, z);
                    this.context.meanTemperature = meanTemperatureRaster.get(x, z);
                    this.context.cover = coverRaster.get(x, z);
                    this.context.landform = landformRaster.get(x, z);

                    this.biomeBuffer[x + z * 16] = BiomeClassifier.classify(this.context);
                }
            }

            return this.biomeBuffer;
        }).orElseGet(() -> ArrayUtils.fill(this.biomeBuffer, Biomes.DEFAULT));
    }
}
