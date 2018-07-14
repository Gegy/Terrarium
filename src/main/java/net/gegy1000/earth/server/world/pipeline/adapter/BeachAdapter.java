package net.gegy1000.earth.server.world.pipeline.adapter;

import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRasterTile;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;
import net.gegy1000.terrarium.server.world.region.GenerationRegion;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorImproved;

import java.util.Arrays;
import java.util.Random;

public class BeachAdapter implements RegionAdapter {
    private static final double FREQ = 0.2;

    private final RegionComponentType<CoverRasterTile> coverComponent;
    private final RegionComponentType<WaterRasterTile> waterComponent;
    private final int beachSize;

    private final CoverType beachCover;

    private final NoiseGeneratorImproved beachNoise;
    private final double[] beachWeight = new double[GenerationRegion.BUFFERED_SIZE * GenerationRegion.BUFFERED_SIZE];

    public BeachAdapter(World world, RegionComponentType<CoverRasterTile> coverComponent, RegionComponentType<WaterRasterTile> waterComponent, int beachSize, CoverType beachCover) {
        this.coverComponent = coverComponent;
        this.waterComponent = waterComponent;
        this.beachSize = beachSize;
        this.beachCover = beachCover;

        Random random = new Random(world.getWorldInfo().getSeed());
        this.beachNoise = new NoiseGeneratorImproved(random);
    }

    @Override
    public void adapt(RegionData data, int x, int z, int width, int height) {
        CoverRasterTile coverTile = data.getOrExcept(this.coverComponent);
        WaterRasterTile waterTile = data.getOrExcept(this.waterComponent);

        if (this.beachSize <= 0) {
            return;
        }

        Arrays.fill(this.beachWeight, 0.0);
        this.beachNoise.populateNoiseArray(this.beachWeight, x * FREQ, 0.0, z * FREQ, width, 1, height, FREQ, 1.0, FREQ, 1.0);

        this.detectEdgesX(width, height, waterTile);
        this.detectEdgesY(width, height, waterTile);

        this.applyBeaches(width, height, coverTile, waterTile);
    }

    private void detectEdgesX(int width, int height, WaterRasterTile waterTile) {
        for (int localY = 0; localY < height; localY++) {
            int lastWaterType = waterTile.getWaterType(0, localY);
            for (int localX = 1; localX < width; localX++) {
                int waterType = waterTile.getWaterType(localX, localY);
                if (lastWaterType != waterType) {
                    this.spreadBeach(this.beachSize - 1, width, height, localX, localY);
                    lastWaterType = waterType;
                }
            }
        }
    }

    private void detectEdgesY(int width, int height, WaterRasterTile waterTile) {
        for (int localX = 0; localX < width; localX++) {
            int lastWaterType = waterTile.getWaterType(localX, 0);
            for (int localY = 1; localY < height; localY++) {
                int waterType = waterTile.getWaterType(localX, localY);
                if (lastWaterType != waterType) {
                    this.spreadBeach(this.beachSize - 1, width, height, localX, localY);
                    lastWaterType = waterType;
                }
            }
        }
    }

    private void spreadBeach(int beachSize, int width, int height, int localX, int localY) {
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

    private void applyBeaches(int width, int height, CoverRasterTile coverTile, WaterRasterTile waterTile) {
        for (int localY = 0; localY < height; localY++) {
            for (int localX = 0; localX < width; localX++) {
                double weight = this.beachWeight[localY + localX * width];
                if (weight > 1.0) {
                    if (WaterRasterTile.isLand(waterTile.getShort(localX, localY))) {
                        coverTile.set(localX, localY, this.beachCover);
                    }
                }
            }
        }
    }
}
