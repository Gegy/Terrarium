package net.gegy1000.terrarium.server.map.system.sampler;

import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;

public class SlopeSampler implements DataSampler<byte[]> {
    private final HeightSampler heightSampler;

    public SlopeSampler(HeightSampler heightSampler) {
        this.heightSampler = heightSampler;
    }

    @Override
    public byte[] sample(EarthGenerationSettings settings, int x, int z, int width, int height) {
        byte[] output = new byte[width * height];

        int sampleWidth = width + 2;
        int sampleHeight = height + 2;
        short[] sampled = this.heightSampler.sample(settings, x - 1, z - 1, sampleWidth, sampleHeight);

        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                int sampleIndex = localX + 1 + (localZ + 1) * sampleWidth;

                short current = sampled[sampleIndex];

                int left = Math.abs(current - sampled[sampleIndex - 1]);
                int right = Math.abs(current - sampled[sampleIndex + 1]);
                int top = Math.abs(current - sampled[sampleIndex - sampleWidth]);
                int bottom = Math.abs(current - sampled[sampleIndex + sampleWidth]);

                int maxSlope = Math.max(left, Math.max(right, Math.max(top, bottom)));
                output[localX + localZ * width] = (byte) MathHelper.clamp(maxSlope, 0, 255);
            }
        }

        return output;
    }
}
