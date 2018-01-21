package net.gegy1000.terrarium.server.map.adapter;

import net.gegy1000.terrarium.server.map.RegionData;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.util.FloodFill;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class WaterFlattenAdapter implements RegionAdapter {
    private final int flattenRange;

    public WaterFlattenAdapter(int flattenRange) {
        this.flattenRange = flattenRange;
    }

    @Override
    public void adapt(EarthGenerationSettings settings, RegionData data, int x, int z, int width, int height) {
        short[] heightBuffer = data.getHeights();
        CoverType[] coverBuffer = data.getCover();

        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                if (coverBuffer[localX + localZ * width] == CoverType.WATER) {
                    AverageGlobHeightVisitor visitor = new AverageGlobHeightVisitor(heightBuffer, width);
                    FloodFill.floodVisit(coverBuffer, width, height, new FloodFill.Point(localX, localZ), visitor);

                    List<FloodFill.Point> waterPoints = visitor.getVisitedPoints();
                    short averageHeight = visitor.getAverageHeight();
                    this.flattenArea(waterPoints, averageHeight, heightBuffer, coverBuffer, width, height);
                }
            }
        }

        for (int i = 0; i < coverBuffer.length; i++) {
            if (coverBuffer[i] == CoverType.PROCESSING) {
                coverBuffer[i] = CoverType.WATER;
            }
        }
    }

    private void flattenArea(List<FloodFill.Point> waterPoints, short targetHeight, short[] heightBuffer, CoverType[] globBuffer, int width, int height) {
        Set<FloodFill.Point> sourcePoints = new HashSet<>();

        for (FloodFill.Point point : waterPoints) {
            int x = point.getX();
            int z = point.getY();
            int index = x + z * width;

            heightBuffer[index] = targetHeight;

            boolean canEffect = true;
            for (FloodFill.Point sourcePoint : sourcePoints) {
                if (Math.abs(point.getX() - sourcePoint.getX()) + Math.abs(point.getY() - sourcePoint.getY()) < this.flattenRange) {
                    canEffect = false;
                    break;
                }
            }

            if (canEffect && this.hasNeighbouringLand(x, z, globBuffer, width, height)) {
                sourcePoints.add(point);
                AffectAreaVisitor visitor = new AffectAreaVisitor(point, this.flattenRange, targetHeight);
                FloodFill.floodVisit(heightBuffer, width, height, point, visitor);
            }
        }
    }

    private boolean hasNeighbouringLand(int x, int z, CoverType[] globBuffer, int width, int height) {
        int index = x + z * width;
        return (x > 0 && globBuffer[index - 1] != CoverType.PROCESSING)
                || (x < width - 1 && globBuffer[index + 1] != CoverType.PROCESSING)
                || (z > 0 && globBuffer[index - width] != CoverType.PROCESSING)
                || (z < height - 1 && globBuffer[index + width] != CoverType.PROCESSING);
    }

    private class AverageGlobHeightVisitor implements FloodFill.Visitor<CoverType> {
        private final short[] heightBuffer;
        private final int width;

        private final List<FloodFill.Point> visitedPoints = new LinkedList<>();

        private long totalHeight;

        private AverageGlobHeightVisitor(short[] heightBuffer, int width) {
            this.heightBuffer = heightBuffer;
            this.width = width;
        }

        @Override
        public CoverType visit(FloodFill.Point point, CoverType sampled) {
            this.totalHeight += this.heightBuffer[point.getX() + point.getY() * this.width];
            this.visitedPoints.add(point);
            return CoverType.PROCESSING;
        }

        @Override
        public boolean canVisit(FloodFill.Point point, CoverType sampled) {
            return sampled == CoverType.WATER;
        }

        public short getAverageHeight() {
            return (short) (this.totalHeight / this.visitedPoints.size());
        }

        public List<FloodFill.Point> getVisitedPoints() {
            return this.visitedPoints;
        }
    }

    private class AffectAreaVisitor implements FloodFill.ShortVisitor {
        private final FloodFill.Point origin;

        private final int range;
        private final short target;

        private AffectAreaVisitor(FloodFill.Point origin, int range, short target) {
            this.origin = origin;

            this.range = range;
            this.target = target;
        }

        @Override
        public short visit(FloodFill.Point point, short sampled) {
            int deltaX = point.getX() - this.origin.getX();
            int deltaZ = point.getY() - this.origin.getY();
            double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            if (distance <= 1.5) {
                return this.target;
            }
            double scale = MathHelper.clamp(distance / this.range, 0.0, 1.0);
            return (short) MathHelper.floor(this.target + (sampled - this.target) * scale);
        }

        @Override
        public boolean canVisit(FloodFill.Point point, short sampled) {
            if (sampled == this.target) {
                return false;
            }
            int deltaX = Math.abs(point.getX() - this.origin.getX());
            int deltaZ = Math.abs(point.getY() - this.origin.getY());
            return deltaX <= this.range && deltaZ <= this.range;
        }
    }
}
