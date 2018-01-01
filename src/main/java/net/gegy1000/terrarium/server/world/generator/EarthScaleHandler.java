package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;

public class EarthScaleHandler {
    private final EarthGenerationSettings settings;

    private final double heightScale;

    public EarthScaleHandler(EarthGenerationSettings settings) {
        this.settings = settings;

        this.heightScale = this.settings.worldScale * this.settings.terrainHeightScale;
    }

    public void scaleHeightRegion(short[] scaledResult, short[] sample, int sampleSize, Coordinate regionSize) {
        if (Math.abs(regionSize.getGlobalX() - regionSize.getGlobalZ()) > 1e-4) {
            throw new IllegalArgumentException("Cannot scale region where width != height");
        }

        double scale = (sampleSize - 1.0) / (regionSize.getBlockZ());
        int scaledSize = MathHelper.floor(regionSize.getBlockX());

        for (int scaledZ = 0; scaledZ < scaledSize; scaledZ++) {
            double sampleZ = scaledZ * scale;
            int originZ = MathHelper.floor(sampleZ);
            double intermediateZ = sampleZ - originZ;

            for (int scaledX = 0; scaledX < scaledSize; scaledX++) {
                double sampleX = scaledX * scale;
                int originX = MathHelper.floor(sampleX);
                double intermediateX = sampleX - originX;

                int sampleIndex = originX + originZ * sampleSize;

                double current = sample[sampleIndex];
                double south = sample[sampleIndex + sampleSize];
                double east = sample[sampleIndex + 1];
                double southEast = sample[sampleIndex + sampleSize + 1];

                double y1 = Interpolation.cosine(current, south, intermediateZ);
                double y2 = Interpolation.cosine(east, southEast, intermediateZ);

                double interpolatedHeight = Interpolation.cosine(y1, y2, intermediateX);
                short scaled = (short) (interpolatedHeight * this.heightScale);

                int resultIndex = scaledX + scaledZ * scaledSize;
                if (interpolatedHeight >= 0.0 && scaled < 1) {
                    scaledResult[resultIndex] = 1;
                } else {
                    scaledResult[resultIndex] = scaled;
                }
            }
        }
    }

    public void scaleGlobRegion(GlobType[] scaledResult, GlobType[] sample, int sampleSize, Coordinate regionSize) {
        if (Math.abs(regionSize.getGlobalX() - regionSize.getGlobalZ()) > 1e-4) {
            throw new IllegalArgumentException("Cannot scale region where width != height");
        }

        double scale = (sampleSize - 1.0) / (regionSize.getBlockZ());
        int scaledSize = MathHelper.floor(regionSize.getBlockX());

        for (int scaledZ = 0; scaledZ < scaledSize; scaledZ++) {
            int originZ = MathHelper.floor(scaledZ * scale);

            for (int scaledX = 0; scaledX < scaledSize; scaledX++) {
                int originX = MathHelper.floor(scaledX * scale);

                scaledResult[scaledX + scaledZ * scaledSize] = sample[originX + originZ * sampleSize];
            }
        }
    }
}
