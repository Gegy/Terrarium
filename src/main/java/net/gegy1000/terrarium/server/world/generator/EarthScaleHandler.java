package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;

public class EarthScaleHandler {
    private final EarthGenerationSettings settings;

    private final int maxHeight;

    private final double heightScale;

    public EarthScaleHandler(EarthGenerationSettings settings, int maxHeight) {
        this.settings = settings;

        this.maxHeight = maxHeight;

        this.heightScale = this.settings.worldScale * this.settings.terrainHeightScale;
    }

    public void scaleHeightRegion(short[] result, short[] sample, int width, int height, int scaledWidth, int scaledHeight) {
        double scale = this.settings.getInverseScale();

        for (int localZ = 0; localZ < scaledHeight; localZ++) {
            double scaledZ = localZ * scale;
            int originZ = MathHelper.floor(scaledZ);
            double intermediateZ = scaledZ - originZ;

            for (int localX = 0; localX < scaledWidth; localX++) {
                double scaledX = localX * scale;
                int originX = MathHelper.floor(scaledX);
                double intermediateX = scaledX - originX;

                int sampleIndex = originX + originZ * width;

                double current = sample[sampleIndex];
                double south = sample[sampleIndex + width];
                double east = sample[sampleIndex + 1];
                double southEast = sample[sampleIndex + width + 1];

                double y1 = Interpolation.cosine(current, south, intermediateZ);
                double y2 = Interpolation.cosine(east, southEast, intermediateZ);

                double interpolated = Interpolation.cosine(y1, y2, intermediateX);
                int scaled = (int) (interpolated * this.heightScale);

                int resultIndex = localX + localZ * scaledWidth;
                if (interpolated >= 0.0 && scaled < 1) {
                    result[resultIndex] = (short) (this.settings.heightOffset + 1);
                } else {
                    result[resultIndex] = (short) MathHelper.clamp(scaled + this.settings.heightOffset, 0, this.maxHeight);
                }
            }
        }
    }

    public void scaleGlobRegion(GlobType[] result, GlobType[] sample, int width, int height, int scaledWidth, int scaledHeight) {
        double scale = this.settings.getInverseScale();

        for (int localZ = 0; localZ < scaledHeight; localZ++) {
            int originZ = MathHelper.floor(localZ * scale);

            for (int localX = 0; localX < scaledWidth; localX++) {
                int originX = MathHelper.floor(localX * scale);

                result[localX + localZ * scaledWidth] = sample[originX + originZ * width];
            }
        }
    }
}
