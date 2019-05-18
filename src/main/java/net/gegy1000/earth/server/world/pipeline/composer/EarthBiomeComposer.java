package net.gegy1000.earth.server.world.pipeline.composer;

import net.gegy1000.cubicglue.util.PseudoRandomMap;
import net.gegy1000.earth.server.world.biome.BiomeClassification;
import net.gegy1000.earth.server.world.biome.BiomeClassifier;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.cover.CoverConfig;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.pipeline.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnData;
import net.gegy1000.terrarium.server.world.pipeline.data.DataKey;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ObjRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Optional;

public final class EarthBiomeComposer implements BiomeComposer {
    private static final long TEMPERATURE_SEED = 6947193999621861488L;
    private static final long RAINFALL_SEED = 3723149413174831639L;

    private final DataKey<ObjRaster<Cover>> coverKey;
    private final DataKey<FloatRaster> temperatureKey;
    private final DataKey<ShortRaster> rainfallKey;

    private final PseudoRandomMap temperatureNoise;
    private final PseudoRandomMap rainfallNoise;

    private final Biome[] biomeBuffer = new Biome[16 * 16];

    public EarthBiomeComposer(
            World world,
            DataKey<ObjRaster<Cover>> coverKey,
            DataKey<FloatRaster> temperatureKey,
            DataKey<ShortRaster> rainfallKey
    ) {
        this.coverKey = coverKey;
        this.temperatureKey = temperatureKey;
        this.rainfallKey = rainfallKey;

        long seed = world.getWorldInfo().getSeed();
        this.temperatureNoise = new PseudoRandomMap(seed, TEMPERATURE_SEED);
        this.rainfallNoise = new PseudoRandomMap(seed, RAINFALL_SEED);
    }

    @Override
    public Biome[] composeBiomes(ColumnData data, ChunkPos columnPos) {
        Optional<ObjRaster<Cover>> coverOption = data.get(this.coverKey);
        Optional<FloatRaster> temperatureOption = data.get(this.temperatureKey);
        Optional<ShortRaster> rainfallOption = data.get(this.rainfallKey);

        if (!coverOption.isPresent() || !temperatureOption.isPresent() || !rainfallOption.isPresent()) {
            return ArrayUtils.fill(this.biomeBuffer, Biomes.DEFAULT);
        }

        ObjRaster<Cover> coverRaster = coverOption.get();
        FloatRaster temperatureRaster = temperatureOption.get();
        ShortRaster rainfallRaster = rainfallOption.get();

        int globalX = columnPos.getXStart();
        int globalZ = columnPos.getZStart();

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                BiomeClassification classification = BiomeClassification.take();

                this.rainfallNoise.initPosSeed(globalX + localX, globalZ + localZ);

                double rainfallNoise = this.noiseOffset(this.rainfallNoise, 100.0);
                short rainfall = (short) (rainfallRaster.get(localX, localZ) + rainfallNoise);
                BiomeClassifier.classifyRainfall(classification, rainfall);

                this.temperatureNoise.initPosSeed(globalX + localX, globalZ + localZ);

                double temperatureNoise = this.noiseOffset(this.temperatureNoise, 1.0);
                float temperature = (float) (temperatureRaster.get(localX, localZ) + temperatureNoise);
                BiomeClassifier.classifyTemperature(classification, temperature);

                Cover cover = coverRaster.get(localX, localZ);
                CoverConfig coverConfig = cover.getConfig();
                coverConfig.classifications().forEach(classification::include);

                this.biomeBuffer[localX + localZ * 16] = classification.match();

                classification.release();
            }
        }

        return this.biomeBuffer;
    }

    private double noiseOffset(PseudoRandomMap noise, double radius) {
        return (noise.nextDouble() * 2.0F - 1.0F) * radius;
    }
}
