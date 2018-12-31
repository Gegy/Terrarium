package net.gegy1000.earth.server.world.pipeline.adapter;

import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.BiomeRasterTile;
import net.gegy1000.terrarium.server.world.region.GenerationRegion;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Arrays;
import java.util.Random;

public class BeachAdapter implements RegionAdapter {
    private static final double FREQ = 0.2;

    private final RegionComponentType<BiomeRasterTile> biomeComponent;
    private final RegionComponentType<WaterRasterTile> waterComponent;
    private final int beachSize;

    private final Biome beachBiome;

    private final PerlinNoiseSampler beachNoise;
    private final double[] beachWeight = new double[GenerationRegion.BUFFERED_SIZE * GenerationRegion.BUFFERED_SIZE];

    public BeachAdapter(World world, RegionComponentType<BiomeRasterTile> biomeComponent, RegionComponentType<WaterRasterTile> waterComponent, int beachSize, Biome beachBiome) {
        this.biomeComponent = biomeComponent;
        this.waterComponent = waterComponent;
        this.beachSize = beachSize;
        this.beachBiome = beachBiome;

        Random random = new Random(world.getLevelProperties().getSeed());
        this.beachNoise = new PerlinNoiseSampler(random);
    }

    @Override
    public void adapt(RegionData data, int x, int z, int width, int height) {
        BiomeRasterTile coverTile = data.getOrExcept(this.biomeComponent);
        WaterRasterTile waterTile = data.getOrExcept(this.waterComponent);

        if (this.beachSize <= 0) {
            return;
        }

        Arrays.fill(this.beachWeight, 0.0);
        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                double noise = this.beachNoise.sample(x * FREQ, 0.0, z * FREQ, 0.0, 0.0);
                this.beachWeight[localX + localZ * width] = noise;
            }
        }

        this.detectEdgesX(waterTile, coverTile, width, height);
        this.detectEdgesY(waterTile, coverTile, width, height);

        this.applyBeaches(width, height, coverTile, waterTile);
    }

    private void detectEdgesX(WaterRasterTile waterTile, BiomeRasterTile coverTile, int width, int height) {
        for (int localY = 0; localY < height; localY++) {
            int lastWaterType = waterTile.getWaterType(0, localY);
            for (int localX = 1; localX < width; localX++) {
                int waterType = waterTile.getWaterType(localX, localY);
                if (lastWaterType != waterType) {
                    this.spreadBeach(coverTile, this.beachSize - 1, localX, localY, width, height);
                    lastWaterType = waterType;
                }
            }
        }
    }

    private void detectEdgesY(WaterRasterTile waterTile, BiomeRasterTile biomeTIle, int width, int height) {
        for (int localX = 0; localX < width; localX++) {
            int lastWaterType = waterTile.getWaterType(localX, 0);
            for (int localY = 1; localY < height; localY++) {
                int waterType = waterTile.getWaterType(localX, localY);
                if (lastWaterType != waterType) {
                    this.spreadBeach(biomeTIle, this.beachSize - 1, localX, localY, width, height);
                    lastWaterType = waterType;
                }
            }
        }
    }

    private void spreadBeach(BiomeRasterTile biomeTile, int beachSize, int localX, int localY, int width, int height) {
        if (this.hasBeach(biomeTile, localX, localY, width, height)) {
            return;
        }
        double maxWeight = (beachSize * beachSize) * 2;
        for (int beachY = -beachSize; beachY <= beachSize; beachY++) {
            int globalY = localY + beachY;
            if (globalY >= 0 && globalY < height) {
                for (int beachX = -beachSize; beachX <= beachSize; beachX++) {
                    int globalX = localX + beachX;
                    if (globalX >= 0 && globalX < width) {
                        double weight = maxWeight - (beachX * beachX + beachY * beachY);
                        this.beachWeight[globalY + globalX * width] += weight / maxWeight;
                    }
                }
            }
        }
    }

    private boolean hasBeach(BiomeRasterTile biomeTile, int localX, int localY, int width, int height) {
        for (int offsetY = -1; offsetY <= 1; offsetY++) {
            int globalY = localY + offsetY;
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                int globalX = localX + offsetX;
                if (globalX >= 0 && globalY >= 0 && globalX < width && globalY < height) {
                    if (biomeTile.get(globalX, globalY).getCategory() == Biome.Category.BEACH) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void applyBeaches(int width, int height, BiomeRasterTile biomeTile, WaterRasterTile waterTile) {
        for (int localY = 0; localY < height; localY++) {
            for (int localX = 0; localX < width; localX++) {
                double weight = this.beachWeight[localY + localX * width];
                if (weight > 1.0) {
                    if (WaterRasterTile.isLand(waterTile.getShort(localX, localY))) {
                        if (biomeTile.get(localX, localY).getCategory() != Biome.Category.BEACH) {
                            biomeTile.set(localX, localY, this.beachBiome);
                        }
                    }
                }
            }
        }
    }
}
