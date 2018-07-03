package net.gegy1000.earth.server.world.pipeline.adapter;

import net.gegy1000.terrarium.server.util.FloodFill;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.TerrariumCoverTypes;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.minecraft.util.math.MathHelper;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class WaterFlattenAdapter implements RegionAdapter {
    private final RegionComponentType<ShortRasterTile> heightComponent;
    private final RegionComponentType<CoverRasterTile> coverComponent;
    private final int flattenRange;

    private final CoverType waterCoverType;

    public WaterFlattenAdapter(RegionComponentType<ShortRasterTile> heightComponent, RegionComponentType<CoverRasterTile> coverComponent, int flattenRange, CoverType waterCoverType) {
        this.heightComponent = heightComponent;
        this.coverComponent = coverComponent;
        this.flattenRange = flattenRange;
        this.waterCoverType = waterCoverType;
    }

    @Override
    public void adapt(GenerationSettings settings, RegionData data, int x, int z, int width, int height) {
        ShortRasterTile heightTile = data.getOrExcept(this.heightComponent);
        CoverRasterTile coverTile = data.getOrExcept(this.coverComponent);

        short[] heightBuffer = heightTile.getShortData();
        CoverType[] coverBuffer = coverTile.getData();

        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                if (coverBuffer[localX + localZ * width] == this.waterCoverType) {
                    AverageCoverHeightVisitor visitor = new AverageCoverHeightVisitor(heightBuffer, width);
                    FloodFill.floodVisit(coverBuffer, width, height, new FloodFill.Point(localX, localZ), visitor);

                    List<FloodFill.Point> waterPoints = visitor.getVisitedPoints();
                    if (!waterPoints.isEmpty()) {
                        short averageHeight = visitor.getAverageHeight();
                        this.flattenArea(waterPoints, averageHeight, heightBuffer, coverBuffer, width, height);
                    }
                }
            }
        }

        for (int i = 0; i < coverBuffer.length; i++) {
            if (coverBuffer[i] == TerrariumCoverTypes.PLACEHOLDER) {
                coverBuffer[i] = this.waterCoverType;
            }
        }
    }

    private void flattenArea(List<FloodFill.Point> waterPoints, short targetHeight, short[] heightBuffer, CoverType[] coverBuffer, int width, int height) {
        Set<FloodFill.Point> sourcePoints = new HashSet<>();

        for (FloodFill.Point point : waterPoints) {
            int x = point.getX();
            int z = point.getY();
            int index = x + z * width;

            heightBuffer[index] = targetHeight;

            boolean canEffect = true;
            for (FloodFill.Point sourcePoint : sourcePoints) {
                if (Math.abs(point.getX() - sourcePoint.getX()) + Math.abs(point.getY() - sourcePoint.getY()) < 6) {
                    canEffect = false;
                    break;
                }
            }

            if (canEffect && !this.isAlreadyFlattened(x, z, targetHeight, heightBuffer, width, height)
                    && this.hasNeighbouringLand(x, z, coverBuffer, width, height)) {
                sourcePoints.add(point);
                AffectAreaVisitor visitor = new AffectAreaVisitor(point, this.flattenRange, targetHeight);
                FloodFill.floodVisit(heightBuffer, width, height, point, visitor);
            }
        }
    }

    private boolean isAlreadyFlattened(int x, int z, short targetHeight, short[] heightBuffer, int width, int height) {
        int index = x + z * width;
        return (x <= 0 || Math.abs(heightBuffer[index - 1] - targetHeight) > 0)
                && (x >= width - 1 || Math.abs(heightBuffer[index + 1] - targetHeight) > 0)
                && (z <= 0 || Math.abs(heightBuffer[index - width] - targetHeight) > 0)
                && (z >= height - 1 || Math.abs(heightBuffer[index + width] - targetHeight) > 0);
    }

    private boolean hasNeighbouringLand(int x, int z, CoverType[] coverBuffer, int width, int height) {
        int index = x + z * width;
        return (x > 0 && coverBuffer[index - 1] != TerrariumCoverTypes.PLACEHOLDER)
                || (x < width - 1 && coverBuffer[index + 1] != TerrariumCoverTypes.PLACEHOLDER)
                || (z > 0 && coverBuffer[index - width] != TerrariumCoverTypes.PLACEHOLDER)
                || (z < height - 1 && coverBuffer[index + width] != TerrariumCoverTypes.PLACEHOLDER);
    }

    private class AverageCoverHeightVisitor implements FloodFill.Visitor<CoverType> {
        private final short[] heightBuffer;
        private final int width;

        private final List<FloodFill.Point> visitedPoints = new LinkedList<>();

        private long totalHeight;

        private AverageCoverHeightVisitor(short[] heightBuffer, int width) {
            this.heightBuffer = heightBuffer;
            this.width = width;
        }

        @Override
        public CoverType visit(FloodFill.Point point, CoverType sampled) {
            this.totalHeight += this.heightBuffer[point.getX() + point.getY() * this.width];
            this.visitedPoints.add(point);
            return TerrariumCoverTypes.PLACEHOLDER;
        }

        @Override
        public boolean canVisit(FloodFill.Point point, CoverType sampled) {
            return sampled == WaterFlattenAdapter.this.waterCoverType;
        }

        private short getAverageHeight() {
            return (short) (this.totalHeight / this.visitedPoints.size());
        }

        private List<FloodFill.Point> getVisitedPoints() {
            return this.visitedPoints;
        }
    }

    private class AffectAreaVisitor implements FloodFill.ShortVisitor {
        private final FloodFill.Point origin;

        private final int range;
        private final short target;

        private AffectAreaVisitor(FloodFill.Point origin, int range, short target) {
            this.origin = origin;

            this.range = range * range;
            this.target = target;
        }

        @Override
        public short visit(FloodFill.Point point, short sampled) {
            int deltaX = point.getX() - this.origin.getX();
            int deltaZ = point.getY() - this.origin.getY();
            double distance = deltaX * deltaX + deltaZ * deltaZ;
            if (distance <= 5.0 * 5.0) {
                return this.target;
            }
            double scale = MathHelper.clamp(distance / this.range, 0.0, 1.0);
            return (short) MathHelper.floor(this.target + (sampled - this.target) * scale);
        }

        @Override
        public boolean canVisit(FloodFill.Point point, short sampled) {
            if (Math.abs(sampled - this.target) > 0) {
                return false;
            }
            int deltaX = Math.abs(point.getX() - this.origin.getX());
            int deltaZ = Math.abs(point.getY() - this.origin.getY());
            return deltaX <= this.range && deltaZ <= this.range;
        }
    }
}
