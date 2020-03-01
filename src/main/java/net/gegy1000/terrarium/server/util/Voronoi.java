package net.gegy1000.terrarium.server.util;

import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.minecraft.util.math.MathHelper;

public class Voronoi {
    private static final long DISPLACEMENT_SEED = 2016969737595986194L;

    private final SpatialRandom random;

    private final DistanceFunc distanceFunc;
    private final double fuzzRange;

    public Voronoi(DistanceFunc distanceFunc, double fuzzRange, long seed) {
        this.distanceFunc = distanceFunc;
        this.fuzzRange = fuzzRange;

        this.random = new SpatialRandom(seed, DISPLACEMENT_SEED);
    }

    public <T> void scale(T src, T dest, DataView srcView, DataView destView,
                          double scaleX, double scaleY, double offsetX, double offsetY
    ) {
        int width = destView.getWidth();
        int height = destView.getHeight();

        for (int y = 0; y < height; y++) {
            double srcY = y * scaleY + offsetX;

            for (int x = 0; x < width; x++) {
                double srcX = x * scaleX + offsetY;

                int srcIndex = this.getCellIndex(srcView, srcX, srcY);
                int destIndex = x + y * width;

                System.arraycopy(src, srcIndex, dest, destIndex, 1);
            }
        }
    }

    public void scaleBytes(byte[] src, byte[] dest, DataView srcView, DataView destView,
                           double scaleX, double scaleY, double offsetX, double offsetY
    ) {
        int width = destView.getWidth();
        int height = destView.getHeight();

        for (int y = 0; y < height; y++) {
            double srcY = y * scaleY + offsetY;

            for (int x = 0; x < width; x++) {
                double srcX = x * scaleX + offsetX;

                int srcIndex = this.getCellIndex(srcView, srcX, srcY);
                int destIndex = x + y * width;

                dest[destIndex] = src[srcIndex];
            }
        }
    }

    private int getCellIndex(DataView srcView, double x, double y) {
        int originX = MathHelper.floor(x);
        int originY = MathHelper.floor(y);

        int srcWidth = srcView.getWidth();
        int srcHeight = srcView.getHeight();

        int cellIndex = 0;
        double selectionDistance = Double.MAX_VALUE;

        for (int srcY = originY - 1; srcY <= originY + 1; srcY++) {
            for (int srcX = originX - 1; srcX <= originX + 1; srcX++) {
                if (srcX < 0 || srcY < 0 || srcX >= srcWidth || srcY >= srcHeight) {
                    continue;
                }

                this.random.setSeed(srcX + srcView.getX(), srcY + srcView.getY());
                double distance = this.distanceFunc.get(x, y, this.fuzz(srcX), this.fuzz(srcY));
                if (distance < selectionDistance) {
                    selectionDistance = distance;
                    cellIndex = srcX + srcY * srcWidth;
                }
            }
        }

        return cellIndex;
    }

    private double fuzz(double x) {
        double offset = this.random.nextInt(4) / 4.0 - 0.5;
        return (x + 0.5) + (offset * this.fuzzRange);
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
