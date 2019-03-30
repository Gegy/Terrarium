package net.gegy1000.earth.server.world.pipeline.adapter;

import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.UnsignedByteRaster;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorOctaves;

import java.util.Random;

public class SlopeNoiseAdapter implements RegionAdapter {
    private final RegionComponentType<UnsignedByteRaster> slopeComponent;
    private final double scale;

    private final NoiseGeneratorOctaves slopeNoise;

    public SlopeNoiseAdapter(World world, RegionComponentType<UnsignedByteRaster> slopeComponent, double scale) {
        this.slopeComponent = slopeComponent;
        this.scale = scale;

        this.slopeNoise = new NoiseGeneratorOctaves(new Random(world.getWorldInfo().getSeed()), 1);
    }

    @Override
    public void adapt(RegionData data, int x, int z, int width, int height) {
        UnsignedByteRaster slopeTile = data.getOrExcept(this.slopeComponent);

        double[] noise = new double[width * height];
        double frequency = (1.0 / this.scale) * 0.7;
        this.slopeNoise.generateNoiseOctaves(noise, z, x, width, height, frequency, frequency, 0.0);

        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                int slopeNoise = MathHelper.floor(noise[localX + localZ * width] * 35.0);
                int slope = slopeTile.getByte(localX, localZ);
                slopeTile.setByte(localX, localZ, MathHelper.clamp(slope + slopeNoise, 0, 255));
            }
        }
    }
}
