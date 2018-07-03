package net.gegy1000.terrarium.server.world.pipeline.adapter;

import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;
import net.gegy1000.terrarium.server.world.region.GenerationRegion;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorImproved;

import java.util.Random;

public class BeachAdapter implements RegionAdapter {
    private final RegionComponentType<CoverRasterTile> coverComponent;
    private final int beachSize;

    private final CoverType waterCover;
    private final CoverType beachCover;

    private final NoiseGeneratorImproved beachNoise;
    private final double[] sampledBeachNoise = new double[GenerationRegion.BUFFERED_SIZE * GenerationRegion.BUFFERED_SIZE];

    public BeachAdapter(World world, RegionComponentType<CoverRasterTile> coverComponent, int beachSize, CoverType waterCover, CoverType beachCover) {
        this.coverComponent = coverComponent;
        this.beachSize = beachSize;
        this.waterCover = waterCover;
        this.beachCover = beachCover;

        Random random = new Random(world.getWorldInfo().getSeed());
        this.beachNoise = new NoiseGeneratorImproved(random);
    }

    @Override
    public void adapt(GenerationSettings settings, RegionData data, int x, int z, int width, int height) {
        CoverRasterTile coverTile = data.getOrExcept(this.coverComponent);
        if (this.beachSize <= 0) {
            return;
        }

        double frequency = 0.2;
        this.beachNoise.populateNoiseArray(this.sampledBeachNoise, x * frequency, 0.0, z * frequency, width, 1, height, frequency, 1.0, frequency, 1.0);

        CoverType[] coverBuffer = coverTile.getData();

        for (int localY = 0; localY < height; localY++) {
            CoverType last = coverBuffer[localY * width];
            for (int localX = 1; localX < width; localX++) {
                CoverType cover = coverBuffer[localX + localY * width];
                if (last != cover && cover == this.waterCover || last == this.waterCover) {
                    this.spreadBeach(this.beachSize - 1, width, height, localX, localY, coverBuffer);
                }
                last = cover;
            }
        }
    }

    private void spreadBeach(int beachSize, int width, int height, int localX, int localY, CoverType[] coverBuffer) {
        double maxWeight = (beachSize * beachSize) * 2;
        for (int beachY = -beachSize; beachY <= beachSize; beachY++) {
            int globalY = localY + beachY;
            if (globalY >= 0 && globalY < height) {
                for (int beachX = -beachSize; beachX <= beachSize; beachX++) {
                    int globalX = localX + beachX;
                    if (globalX >= 0 && globalX < width) {
                        int beachIndex = globalX + globalY * width;
                        if (coverBuffer[beachIndex] != this.waterCover) {
                            double weight = maxWeight - (beachX * beachX + beachY * beachY);
                            double noise = this.sampledBeachNoise[globalY + globalX * GenerationRegion.BUFFERED_SIZE];
                            if (weight > noise * noise * 3.0) {
                                coverBuffer[beachIndex] = this.beachCover;
                            }
                        }
                    }
                }
            }
        }
    }
}
