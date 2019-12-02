package net.gegy1000.terrarium.server.util;

import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.world.pipeline.data.DataView;
import net.minecraft.util.math.MathHelper;

public class Voronoi {
    private static final long DISPLACEMENT_SEED = 2016969737595986194L;

    private final SpatialRandom random;

    private final DistanceFunc distanceFunc;
    private final double fuzzRange;
    private final int gridSize;

    public Voronoi(DistanceFunc distanceFunc, double fuzzRange, int gridSize, long seed) {
        this.distanceFunc = distanceFunc;
        this.fuzzRange = fuzzRange;
        this.gridSize = gridSize;

        this.random = new SpatialRandom(seed, DISPLACEMENT_SEED);
    }

    public <T> void scale(T input, T output, DataView sourceView, DataView scaledView,
                          double scaleFactorX, double scaleFactorY, double originOffsetX, double originOffsetY
    ) {
        double scaledOffsetX = originOffsetX / scaleFactorX;
        double scaledOffsetY = originOffsetY / scaleFactorY;

        int scaledWidth = scaledView.getWidth();
        int scaledHeight = scaledView.getHeight();

        for (int scaledY = 0; scaledY < scaledHeight; scaledY++) {
            double sampleY = scaledY * scaleFactorY + originOffsetX;
            int originY = MathHelper.floor(sampleY);

            for (int scaledX = 0; scaledX < scaledWidth; scaledX++) {
                double sampleX = scaledX * scaleFactorX + originOffsetY;
                int originX = MathHelper.floor(sampleX);

                int srcIndex = this.getCellIndex(sourceView, originX, originY, scaledX + scaledOffsetX, scaledY + scaledOffsetY, scaleFactorX, scaleFactorY);
                int destIndex = scaledX + scaledY * scaledWidth;

                System.arraycopy(input, srcIndex, output, destIndex, 1);
            }
        }
    }

    private int getCellIndex(DataView sourceView,
                             int originX, int originY, double scaledX, double scaledY,
                             double scaleFactorX, double scaleFactorY
    ) {
        int cellIndex = 0;
        double selectionDistance = Double.MAX_VALUE;
        for (int neighbourY = originY - 1; neighbourY <= originY + 1; neighbourY++) {
            for (int neighbourX = originX - 1; neighbourX <= originX + 1; neighbourX++) {
                this.random.setSeed(neighbourX + sourceView.getX(), neighbourY + sourceView.getY());
                double fuzzedX = this.fuzzPoint(neighbourX) / scaleFactorX;
                double fuzzedY = this.fuzzPoint(neighbourY) / scaleFactorY;
                double distance = this.distanceFunc.get(scaledX, scaledY, fuzzedX, fuzzedY);
                if (distance < selectionDistance) {
                    selectionDistance = distance;
                    cellIndex = this.getClampedIndex(sourceView.getWidth(), sourceView.getHeight(), neighbourX, neighbourY);
                }
            }
        }
        return cellIndex;
    }

    private int getClampedIndex(int width, int height, int x, int y) {
        x = MathHelper.clamp(x, 0, width - 1);
        y = MathHelper.clamp(y, 0, height - 1);
        return x + y * width;
    }

    private double fuzzPoint(double point) {
        double offset = (double) this.random.nextInt(this.gridSize) / this.gridSize;
        return point + 0.5 + (offset - 0.5) * this.fuzzRange;
    }

    public enum DistanceFunc {
        EUCLIDEAN {
            @Override
            public double get(double originX, double originY, double targetX, double targetY) {
                double deltaX = originX - targetX;
                double deltaY = originY - targetY;
                return deltaX * deltaX + deltaY * deltaY;
            }
        },
        MANHATTAN {
            @Override
            public double get(double originX, double originY, double targetX, double targetY) {
                return Math.abs(originX - targetX) + Math.abs(originY - targetY);
            }
        };

        public abstract double get(double originX, double originY, double targetX, double targetY);
    }
}
