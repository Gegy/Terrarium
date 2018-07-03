package net.gegy1000.terrarium.server.util;

import net.minecraft.util.math.MathHelper;

import java.util.Random;

public class Voronoi {
    private static final long PRIME_1 = 22075533469133L;
    private static final long PRIME_2 = 25293517046197L;

    private final Random random;

    private final DistanceFunc distanceFunc;
    private final double fuzzRange;
    private final int gridSize;
    private final long seed;

    public Voronoi(DistanceFunc distanceFunc, double fuzzRange, int gridSize, long seed) {
        this.distanceFunc = distanceFunc;
        this.fuzzRange = fuzzRange;
        this.gridSize = gridSize;

        this.random = new Random(seed);
        this.seed = this.random.nextLong() ^ this.random.nextLong();
    }

    public <T> void scale(T[] input, T[] output, int seedOffsetX, int seedOffsetY,
                         int width, int height, int scaledWidth, int scaledHeight,
                         double scaleFactorX, double scaleFactorY, double originOffsetX, double originOffsetY
    ) {
        double scaledOffsetX = originOffsetX / scaleFactorX;
        double scaledOffsetY = originOffsetY / scaleFactorY;

        for (int scaledY = 0; scaledY < scaledHeight; scaledY++) {
            double sampleY = scaledY * scaleFactorY + originOffsetX;
            int originY = MathHelper.floor(sampleY);

            for (int scaledX = 0; scaledX < scaledWidth; scaledX++) {
                double sampleX = scaledX * scaleFactorX + originOffsetY;
                int originX = MathHelper.floor(sampleX);

                T cellValue = this.getCellValue(input, seedOffsetX, seedOffsetY, originX, originY, scaledX + scaledOffsetX, scaledY + scaledOffsetY, width, height, scaleFactorX, scaleFactorY);
                output[scaledX + scaledY * scaledWidth] = cellValue;
            }
        }
    }

    private <T> T getCellValue(T[] input, int seedOffsetX, int seedOffsetY,
                               int originX, int originY, double scaledX, double scaledY,
                               int width, int height, double scaleFactorX, double scaleFactorY
    ) {
        T cellValue = null;
        double selectionDistance = Double.MAX_VALUE;
        for (int neighbourY = originY - 1; neighbourY <= originY + 1; neighbourY++) {
            for (int neighbourX = originX - 1; neighbourX <= originX + 1; neighbourX++) {
                this.random.setSeed(this.getCellSeed(neighbourX + seedOffsetX, neighbourY + seedOffsetY, this.seed));
                double fuzzedX = this.fuzzPoint(neighbourX) / scaleFactorX;
                double fuzzedY = this.fuzzPoint(neighbourY) / scaleFactorY;
                double distance = this.distanceFunc.get(scaledX, scaledY, fuzzedX, fuzzedY);
                if (distance < selectionDistance) {
                    selectionDistance = distance;
                    cellValue = this.getClamped(input, width, height, neighbourX, neighbourY);
                }
            }
        }
        return cellValue;
    }

    private <T> T getClamped(T[] input, int width, int height, int x, int y) {
        if (x < 0) {
            x = 0;
        } else if (x >= width) {
            x = width - 1;
        }
        if (y < 0) {
            y = 0;
        } else if (y >= height) {
            y = height - 1;
        }
        return input[x + y * width];
    }

    private double fuzzPoint(double point) {
        double offset = (double) this.random.nextInt(this.gridSize) / this.gridSize;
        return point + 0.5 + (offset - 0.5) * this.fuzzRange;
    }

    private long getCellSeed(int x, int y, long seed) {
        return (x * PRIME_1 + y * PRIME_2) ^ seed;
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
