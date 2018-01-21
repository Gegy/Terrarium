package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.util.Voronoi;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;

public class EarthScaleHandler {
    private static final int HEIGHT_BUFFER = 1;

    private static final Voronoi GLOB_VORONOI = new Voronoi(Voronoi.DistanceFunc.EUCLIDEAN, 1.2, 4, 1000);

    private final double heightScale;
    private final double globScale;

    private final int scaledSize;

    private final int heightSampleSize;
    private final int globSampleSize;

    private final double terrainHeightScale;

    public EarthScaleHandler(EarthGenerationSettings settings, Coordinate regionSize, Coordinate bufferedRegionSize) {
        if (Math.abs(regionSize.getGlobalX() - regionSize.getGlobalZ()) > 1e-4) {
            throw new IllegalArgumentException("Cannot scale region where width != height");
        }

        this.heightScale = Math.round(regionSize.getGlobalX()) / regionSize.getBlockX();
        this.globScale = Math.round(regionSize.getGlobX()) / regionSize.getBlockX();

        this.scaledSize = MathHelper.floor(bufferedRegionSize.getBlockX());
        this.heightSampleSize = MathHelper.floor((this.scaledSize - 1) * this.heightScale) + HEIGHT_BUFFER + 1;
        this.globSampleSize = MathHelper.floor((this.scaledSize - 1) * this.globScale) + 1;

        this.terrainHeightScale = settings.worldScale * settings.terrainHeightScale;
    }

    public void scaleHeightRegion(short[] scaledResult, short[] sample) {
        for (int scaledZ = 0; scaledZ < this.scaledSize; scaledZ++) {
            double sampleZ = scaledZ * this.heightScale;
            int originZ = MathHelper.floor(sampleZ);
            double intermediateZ = sampleZ - originZ;

            for (int scaledX = 0; scaledX < this.scaledSize; scaledX++) {
                double sampleX = scaledX * this.heightScale;
                int originX = MathHelper.floor(sampleX);
                double intermediateX = sampleX - originX;

                int sampleIndex = originX + originZ * this.heightSampleSize;

                double current = sample[sampleIndex];
                double south = sample[sampleIndex + this.heightSampleSize];
                double east = sample[sampleIndex + 1];
                double southEast = sample[sampleIndex + this.heightSampleSize + 1];

                double y1 = Interpolation.cosine(current, south, intermediateZ);
                double y2 = Interpolation.cosine(east, southEast, intermediateZ);

                double interpolatedHeight = Interpolation.cosine(y1, y2, intermediateX);
                short scaled = (short) (interpolatedHeight * this.terrainHeightScale);

                int resultIndex = scaledX + scaledZ * this.scaledSize;
                if (interpolatedHeight >= 0.0 && scaled < 1) {
                    scaledResult[resultIndex] = 1;
                } else {
                    scaledResult[resultIndex] = scaled;
                }
            }
        }
    }

    public void scaleGlobRegion(CoverType[] scaledResult, CoverType[] sample) {
        GLOB_VORONOI.scale(sample, scaledResult, this.globSampleSize, this.globSampleSize, this.scaledSize, this.scaledSize);
    }

    public int getHeightSampleSize() {
        return this.heightSampleSize;
    }

    public int getGlobSampleSize() {
        return this.globSampleSize;
    }
}
