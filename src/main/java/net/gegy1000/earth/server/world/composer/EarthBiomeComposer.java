package net.gegy1000.earth.server.world.composer;

import net.gegy1000.earth.server.world.biome.BiomeClassifier;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.pipeline.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnData;
import net.gegy1000.terrarium.server.world.pipeline.data.DataKey;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;

import java.util.Optional;

public final class EarthBiomeComposer implements BiomeComposer {
    private final DataKey<EnumRaster<Cover>> coverKey;
    private final DataKey<EnumRaster<Landform>> landformKey;
    private final DataKey<FloatRaster> temperatureKey;
    private final DataKey<ShortRaster> rainfallKey;

    private final Biome[] biomeBuffer = new Biome[16 * 16];

    private final BiomeClassifier.Context context = new BiomeClassifier.Context();

    public EarthBiomeComposer(
            DataKey<EnumRaster<Cover>> coverKey,
            DataKey<EnumRaster<Landform>> landformKey, DataKey<FloatRaster> temperatureKey,
            DataKey<ShortRaster> rainfallKey
    ) {
        this.coverKey = coverKey;
        this.landformKey = landformKey;
        this.temperatureKey = temperatureKey;
        this.rainfallKey = rainfallKey;
    }

    @Override
    public Biome[] composeBiomes(ColumnData data, ChunkPos columnPos) {
        Optional<EnumRaster<Cover>> coverOption = data.get(this.coverKey);
        Optional<EnumRaster<Landform>> landformOption = data.get(this.landformKey);
        Optional<FloatRaster> temperatureOption = data.get(this.temperatureKey);
        Optional<ShortRaster> rainfallOption = data.get(this.rainfallKey);

        if (!coverOption.isPresent() || !temperatureOption.isPresent() || !rainfallOption.isPresent() || !landformOption.isPresent()) {
            return ArrayUtils.fill(this.biomeBuffer, Biomes.DEFAULT);
        }

        EnumRaster<Cover> coverRaster = coverOption.get();
        EnumRaster<Landform> landformRaster = landformOption.get();
        FloatRaster temperatureRaster = temperatureOption.get();
        ShortRaster rainfallRaster = rainfallOption.get();

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                this.context.annualRainfall = rainfallRaster.get(localX, localZ);
                this.context.averageTemperature = temperatureRaster.get(localX, localZ);
                this.context.cover = coverRaster.get(localX, localZ);
                this.context.landform = landformRaster.get(localX, localZ);

                this.biomeBuffer[localX + localZ * 16] = BiomeClassifier.classify(this.context);
            }
        }

        return this.biomeBuffer;
    }
}
