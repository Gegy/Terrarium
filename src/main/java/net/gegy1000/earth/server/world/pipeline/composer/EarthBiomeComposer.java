package net.gegy1000.earth.server.world.pipeline.composer;

import net.gegy1000.cubicglue.util.PseudoRandomMap;
import net.gegy1000.earth.server.world.biome.BiomeClassification;
import net.gegy1000.earth.server.world.biome.BiomeClassifier;
import net.gegy1000.earth.server.world.cover.CoverClassification;
import net.gegy1000.earth.server.world.cover.CoverConfig;
import net.gegy1000.earth.server.world.pipeline.source.tile.CoverRaster;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public final class EarthBiomeComposer implements BiomeComposer {
    private static final long TEMPERATURE_SEED = 6947193999621861488L;
    private static final long RAINFALL_SEED = 3723149413174831639L;

    private final RegionComponentType<CoverRaster> coverComponent;
    private final RegionComponentType<FloatRaster> temperatureComponent;
    private final RegionComponentType<ShortRaster> rainfallComponent;

    private final PseudoRandomMap temperatureNoise;
    private final PseudoRandomMap rainfallNoise;

    private final Biome[] biomeBuffer = new Biome[16 * 16];

    public EarthBiomeComposer(
            World world,
            RegionComponentType<CoverRaster> coverComponent,
            RegionComponentType<FloatRaster> temperatureComponent,
            RegionComponentType<ShortRaster> rainfallComponent
    ) {
        this.coverComponent = coverComponent;
        this.temperatureComponent = temperatureComponent;
        this.rainfallComponent = rainfallComponent;

        long seed = world.getWorldInfo().getSeed();
        this.temperatureNoise = new PseudoRandomMap(seed, TEMPERATURE_SEED);
        this.rainfallNoise = new PseudoRandomMap(seed, RAINFALL_SEED);
    }

    @Override
    public Biome[] composeBiomes(RegionGenerationHandler regionHandler, int chunkX, int chunkZ) {
        CoverRaster coverRaster = regionHandler.getChunkRaster(this.coverComponent);
        FloatRaster temperatureRaster = regionHandler.getChunkRaster(this.temperatureComponent);
        ShortRaster rainfallRaster = regionHandler.getChunkRaster(this.rainfallComponent);

        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                BiomeClassification classification = BiomeClassification.take();

                this.rainfallNoise.initPosSeed(globalX + localX, globalZ + localZ);

                double rainfallNoise = this.noiseOffset(this.rainfallNoise, 100.0);
                short rainfall = (short) (rainfallRaster.getShort(localX, localZ) + rainfallNoise);
                BiomeClassifier.classifyRainfall(classification, rainfall);

                this.temperatureNoise.initPosSeed(globalX + localX, globalZ + localZ);

                double temperatureNoise = this.noiseOffset(this.temperatureNoise, 1.0);
                float temperature = (float) (temperatureRaster.getFloat(localX, localZ) + temperatureNoise);
                BiomeClassifier.classifyTemperature(classification, temperature);

                CoverClassification cover = coverRaster.get(localX, localZ);
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

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[] { this.temperatureComponent, this.rainfallComponent };
    }
}
