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

    public <T> T[] scale(T[] input, T[] output, int width, int height, int scaledWidth, int scaledHeight) {
        this.random.setSeed(this.seed);

        double scaleX = (double) scaledWidth / width;
        double scaleY = (double) scaledHeight / height;

        double stepX = scaleX / MathHelper.ceil(scaleX);
        double stepY = scaleY / MathHelper.ceil(scaleY);

        for (int y = 0; y < height; y++) {
            double scaledY = y * scaleY;
            for (int x = 0; x < width; x++) {
                double scaledX = x * scaleX;

                for (double localY = scaledY; localY < scaledY + MathHelper.ceil(scaleY); localY += stepY) {
                    int originY = MathHelper.floor(localY);
                    for (double localX = scaledX; localX < scaledX + MathHelper.ceil(scaleX); localX += stepX) {
                        int originX = MathHelper.floor(localX);

                        T cellValue = this.getCellValue(input, width, height, scaleX, scaleY, x, y, localX, localY);
                        output[originX + originY * scaledWidth] = cellValue;
                    }
                }
            }
        }

        return output;
    }

    private <T> T getCellValue(T[] input, int width, int height, double scaleX, double scaleY, int x, int y, double localX, double localY) {
        T cellValue = null;
        double selectionDistance = Double.MAX_VALUE;
        for (int neighbourY = y - 1; neighbourY <= y + 1; neighbourY++) {
            for (int neighbourX = x - 1; neighbourX <= x + 1; neighbourX++) {
                this.random.setSeed(this.getCellSeed(neighbourX, neighbourY, this.seed));
                double fuzzedX = this.fuzzPoint(neighbourX) * scaleX;
                double fuzzedY = this.fuzzPoint(neighbourY) * scaleY;
                // TODO: This *might* need to be origin or things break?
                double distance = this.distanceFunc.get(localX, localY, fuzzedX, fuzzedY);
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
