package net.gegy1000.earth.server.world.pipeline.adapter;

import net.gegy1000.earth.server.world.cover.type.BeachyCover;
import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRaster;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.CoverRaster;
import net.gegy1000.terrarium.server.world.region.GenerationRegion;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorImproved;

import java.util.Arrays;
import java.util.Random;

public class BeachAdapter implements RegionAdapter {
    private static final double FREQ = 0.2;

    private final RegionComponentType<CoverRaster> coverComponent;
    private final RegionComponentType<WaterRaster> waterComponent;
    private final int beachSize;

    private final CoverType beachCover;

    private final NoiseGeneratorImproved beachNoise;
    private final double[] beachWeight = new double[GenerationRegion.BUFFERED_SIZE * GenerationRegion.BUFFERED_SIZE];

    public BeachAdapter(World world, RegionComponentType<CoverRaster> coverComponent, RegionComponentType<WaterRaster> waterComponent, int beachSize, CoverType beachCover) {
        this.coverComponent = coverComponent;
        this.waterComponent = waterComponent;
        this.beachSize = beachSize;
        this.beachCover = beachCover;

        Random random = new Random(world.getWorldInfo().getSeed());
        this.beachNoise = new NoiseGeneratorImproved(random);
    }

    @Override
    public void adapt(RegionData data, int x, int z, int width, int height) {
        CoverRaster coverTile = data.getOrExcept(this.coverComponent);
        WaterRaster waterTile = data.getOrExcept(this.waterComponent);

        if (this.beachSize <= 0) {
            return;
        }

        Arrays.fill(this.beachWeight, 0.0);
        this.beachNoise.populateNoiseArray(this.beachWeight, x * FREQ, 0.0, z * FREQ, width, 1, height, FREQ, 1.0, FREQ, 1.0);

        this.detectEdgesX(waterTile, coverTile, width, height);
        this.detectEdgesY(waterTile, coverTile, width, height);

        this.applyBeaches(width, height, coverTile, waterTile);
    }

    private void detectEdgesX(WaterRaster waterTile, CoverRaster coverTile, int width, int height) {
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

    private void detectEdgesY(WaterRaster waterTile, CoverRaster coverTile, int width, int height) {
        for (int localX = 0; localX < width; localX++) {
            int lastWaterType = waterTile.getWaterType(localX, 0);
            for (int localY = 1; localY < height; localY++) {
                int waterType = waterTile.getWaterType(localX, localY);
                if (lastWaterType != waterType) {
                    this.spreadBeach(coverTile, this.beachSize - 1, localX, localY, width, height);
                    lastWaterType = waterType;
                }
            }
        }
    }

    private void spreadBeach(CoverRaster coverTile, int beachSize, int localX, int localY, int width, int height) {
        if (this.hasBeach(coverTile, localX, localY, width, height)) {
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

    private boolean hasBeach(CoverRaster coverTile, int localX, int localY, int width, int height) {
        for (int offsetY = -1; offsetY <= 1; offsetY++) {
            int globalY = localY + offsetY;
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                int globalX = localX + offsetX;
                if (globalX >= 0 && globalY >= 0 && globalX < width && globalY < height) {
                    if (coverTile.get(globalX, globalY) instanceof BeachyCover) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void applyBeaches(int width, int height, CoverRaster coverTile, WaterRaster waterTile) {
        for (int localY = 0; localY < height; localY++) {
            for (int localX = 0; localX < width; localX++) {
                double weight = this.beachWeight[localY + localX * width];
                if (weight > 1.0) {
                    if (WaterRaster.isLand(waterTile.getShort(localX, localY))) {
                        if (!(coverTile.get(localX, localY) instanceof BeachyCover)) {
                            coverTile.set(localX, localY, this.beachCover);
                        }
                    }
                }
            }
        }
    }
}
