package net.gegy1000.earth.server.world.composer;

import com.google.common.collect.ImmutableSet;
import dev.gegy.gengen.util.SpatialRandom;
import net.daporkchop.fp2.mode.heightmap.HeightmapData;
import net.daporkchop.fp2.mode.heightmap.HeightmapPos;
import net.daporkchop.fp2.mode.heightmap.HeightmapTile;
import net.daporkchop.fp2.util.Constants;
import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.world.EarthData;
import net.gegy1000.earth.server.world.biome.BiomeClassifier;
import net.gegy1000.earth.server.world.composer.surface.TerrainSurfaceComposer;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.soil.SoilSelector;
import net.gegy1000.earth.server.world.ecology.soil.SoilTexture;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.composer.RoughHeightmapComposer;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;

import java.util.Collection;

public final class EarthRoughHeightmapComposer implements RoughHeightmapComposer {
    private final GrowthPredictors.Sampler predictorSampler = GrowthPredictors.sampler();

    private final int seaLevel;

    public EarthRoughHeightmapComposer(int seaLevel) {
        this.seaLevel = seaLevel;
    }

    @Override
    public void compose(TerrariumWorld terrarium, ColumnData data, HeightmapPos pos, HeightmapTile tile) {
        EarthWorld earth = EarthWorld.get(terrarium.getWorld());
        if (earth == null) {
            return;
        }

        BiomeClassifier biomeClassifier = earth.getBiomeClassifier();

        ShortRaster heightRaster = data.getOrDefault(EarthData.TERRAIN_HEIGHT);

        HeightmapData value = new HeightmapData();
        GrowthPredictors predictors = new GrowthPredictors();
        SpatialRandom random = new SpatialRandom(terrarium.getWorld(), TerrainSurfaceComposer.SEED);

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        int blockX = pos.blockX();
        int blockZ = pos.blockZ();
        int level = pos.level();

        for (int tileZ = 0; tileZ < 16; tileZ++) {
            for (int tileX = 0; tileX < 16; tileX++) {
                int x = tileX << level;
                int z = tileZ << level;

                int worldY = heightRaster.get(x, z);
                int worldX = x + blockX;
                int worldZ = z + blockZ;
                mutablePos.setPos(worldX, worldY, worldZ);

                random.setSeed(x, worldY & ~15, z);

                this.predictorSampler.sampleTo(data, x, z, predictors);
                Biome biome = biomeClassifier.classify(predictors);

                SoilTexture soilTexture = SoilSelector.select(predictors);
                IBlockState block = soilTexture.sample(random, mutablePos, predictors.slope, 0);

                int skyLight = 15 - MathHelper.clamp(this.seaLevel - worldY, 0, 5) * 3;
                int biomeId = Biome.getIdForBiome(biome);

                value.height = worldY;
                value.state = Block.getStateId(block);
                value.light = skyLight << 4;
                value.biome = biomeId;
                value.waterLight = Constants.packCombinedLight(15 << 20);
                value.waterBiome = biomeId;

                tile.set(tileX, tileZ, value);
            }
        }
    }

    @Override
    public Collection<DataKey<?>> getRequiredData() {
        return ImmutableSet.of(
                EarthData.TERRAIN_HEIGHT,
                EarthData.ELEVATION_METERS,
                EarthData.ANNUAL_RAINFALL, EarthData.MEAN_TEMPERATURE, EarthData.MIN_TEMPERATURE,
                EarthData.CATION_EXCHANGE_CAPACITY, EarthData.ORGANIC_CARBON_CONTENT, EarthData.SOIL_PH,
                EarthData.CLAY_CONTENT, EarthData.SILT_CONTENT, EarthData.SAND_CONTENT,
                EarthData.SLOPE, EarthData.COVER, EarthData.SOIL_SUBORDER, EarthData.LANDFORM
        );
    }
}
