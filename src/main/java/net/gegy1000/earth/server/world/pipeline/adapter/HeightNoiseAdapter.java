package net.gegy1000.earth.server.world.pipeline.adapter;

import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.minecraft.util.math.noise.OctaveSimplexNoiseSampler;
import net.minecraft.world.World;

import java.util.Random;

public class HeightNoiseAdapter implements RegionAdapter {
    private final RegionComponentType<ShortRasterTile> heightComponent;
    private final RegionComponentType<WaterRasterTile> waterComponent;

    private final OctaveSimplexNoiseSampler heightNoise;
    private final double noiseMax;
    private final double noiseScaleXZ;
    private final double noiseScaleY;

    public HeightNoiseAdapter(World world, RegionComponentType<ShortRasterTile> heightComponent, RegionComponentType<WaterRasterTile> waterComponent, int octaveCount, double noiseScaleXZ, double noiseScaleY) {
        this.heightComponent = heightComponent;
        this.waterComponent = waterComponent;

        Random random = new Random(world.getLevelProperties().getSeed());
        this.heightNoise = new OctaveSimplexNoiseSampler(random, octaveCount);

        double max = 0.0;
        double scale = 1.0;
        for (int i = 0; i < octaveCount; i++) {
            max += scale * 2;
            scale /= 2.0;
        }
        this.noiseMax = max;

        this.noiseScaleXZ = noiseScaleXZ;
        this.noiseScaleY = noiseScaleY;
    }

    @Override
    public void adapt(RegionData data, int x, int z, int width, int height) {
        ShortRasterTile heightTile = data.getOrExcept(this.heightComponent);
        WaterRasterTile waterTile = data.getOrExcept(this.waterComponent);

        short[] heightBuffer = heightTile.getShortData();
        short[] waterBuffer = waterTile.getShortData();

        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                int index = localX + localZ * width;
                if (WaterRasterTile.isLand(waterBuffer[index])) {
                    double sampleX = (x + localX) * this.noiseScaleXZ;
                    double sampleZ = (z + localZ) * this.noiseScaleXZ;
                    double noise = this.heightNoise.sample(sampleX, sampleZ);
                    heightBuffer[index] += (noise + this.noiseMax) / (this.noiseMax * 2.0) * this.noiseScaleY * 35.0;
                }
            }
        }
    }
}
