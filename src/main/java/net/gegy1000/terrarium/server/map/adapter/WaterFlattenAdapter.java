package net.gegy1000.terrarium.server.map.adapter;

import net.gegy1000.terrarium.server.map.RegionData;
import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.util.FloodFill;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;

import java.util.LinkedList;
import java.util.List;

public class WaterFlattenAdapter implements RegionAdapter {
    @Override
    public void adapt(EarthGenerationSettings settings, RegionData data, int x, int z, int width, int height) {
        short[] heightBuffer = data.getHeights();
        GlobType[] globBuffer = data.getGlobcover();

        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                if (globBuffer[localX + localZ * width] == GlobType.WATER) {
                    AverageGlobHeightVisitor visitor = new AverageGlobHeightVisitor(heightBuffer, width);
                    FloodFill.floodVisit(globBuffer, width, height, new FloodFill.Point(localX, localZ), visitor);

                    List<FloodFill.Point> waterPoints = visitor.getVisitedPoints();
                    short averageHeight = visitor.getAverageHeight();
                    this.flattenArea(waterPoints, averageHeight, heightBuffer, width, height);
                }
            }
        }

        for (int i = 0; i < globBuffer.length; i++) {
            if (globBuffer[i] == GlobType.PROCESSING) {
                globBuffer[i] = GlobType.WATER;
            }
        }
    }

    private void flattenArea(List<FloodFill.Point> waterPoints, short targetHeight, short[] heightBuffer, int width, int height) {
        for (FloodFill.Point point : waterPoints) {
            int x = point.getX();
            int z = point.getZ();
            int index = x + z * width;

            heightBuffer[index] = targetHeight;

            AffectAreaVisitor visitor = new AffectAreaVisitor(point, 16, targetHeight);
            FloodFill.floodVisit(heightBuffer, width, height, point, visitor);
        }
    }

    private class AverageGlobHeightVisitor implements FloodFill.Visitor<GlobType> {
        private final short[] heightBuffer;
        private final int width;

        private final List<FloodFill.Point> visitedPoints = new LinkedList<>();

        private long totalHeight;

        private AverageGlobHeightVisitor(short[] heightBuffer, int width) {
            this.heightBuffer = heightBuffer;
            this.width = width;
        }

        @Override
        public GlobType visit(FloodFill.Point point, GlobType sampled) {
            this.totalHeight += this.heightBuffer[point.getX() + point.getZ() * this.width];
            this.visitedPoints.add(point);
            return GlobType.PROCESSING;
        }

        @Override
        public boolean canVisit(FloodFill.Point point, GlobType sampled) {
            return sampled == GlobType.WATER;
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
        private final int rangeSquared;
        private final short target;

        private AffectAreaVisitor(FloodFill.Point origin, int range, short target) {
            this.origin = origin;
            this.range = range;
            this.rangeSquared = range * range;
            this.target = target;
        }

        @Override
        public short visit(FloodFill.Point point, short sampled) {
            int deltaX = point.getX() - this.origin.getX();
            int deltaZ = point.getZ() - this.origin.getZ();
            double distance = (double) (deltaX * deltaX + deltaZ * deltaZ);
            double scale = MathHelper.clamp(distance / this.rangeSquared, 0.0, 1.0);
            return (short) MathHelper.ceil(this.target + (sampled - this.target) * scale);
        }

        @Override
        public boolean canVisit(FloodFill.Point point, short sampled) {
            if (sampled == this.target) {
                return false;
            }
            int deltaX = Math.abs(point.getX() - this.origin.getX());
            int deltaZ = Math.abs(point.getZ() - this.origin.getZ());
            return deltaX <= this.range && deltaZ <= this.range;
        }
    }
}
