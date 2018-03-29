package net.gegy1000.earth.server.world.pipeline.adapter;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.util.FloodFill;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.CoverRegistry;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTileAccess;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTileAccess;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class WaterFlattenAdapter implements RegionAdapter {
    private final RegionComponentType<ShortRasterTileAccess> heightComponent;
    private final RegionComponentType<CoverRasterTileAccess> coverComponent;
    private final int flattenRange;

    private final CoverType waterCoverType;

    public WaterFlattenAdapter(RegionComponentType<ShortRasterTileAccess> heightComponent, RegionComponentType<CoverRasterTileAccess> coverComponent, int flattenRange, CoverType waterCoverType) {
        this.heightComponent = heightComponent;
        this.coverComponent = coverComponent;
        this.flattenRange = flattenRange;
        this.waterCoverType = waterCoverType;
    }

    @Override
    public void adapt(GenerationSettings settings, RegionData data, int x, int z, int width, int height) {
        ShortRasterTileAccess heightTile = data.getOrExcept(this.heightComponent);
        CoverRasterTileAccess coverTile = data.getOrExcept(this.coverComponent);

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
            if (coverBuffer[i] == CoverRegistry.PLACEHOLDER) {
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
        return (x > 0 && coverBuffer[index - 1] != CoverRegistry.PLACEHOLDER)
                || (x < width - 1 && coverBuffer[index + 1] != CoverRegistry.PLACEHOLDER)
                || (z > 0 && coverBuffer[index - width] != CoverRegistry.PLACEHOLDER)
                || (z < height - 1 && coverBuffer[index + width] != CoverRegistry.PLACEHOLDER);
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
            return CoverRegistry.PLACEHOLDER;
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

    public static class Parser implements InstanceObjectParser<RegionAdapter> {
        @Override
        public RegionAdapter parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) {
            RegionComponentType<ShortRasterTileAccess> heightComponent = valueParser.parseComponentType(objectRoot, "height_component", ShortRasterTileAccess.class);
            RegionComponentType<CoverRasterTileAccess> coverComponent = valueParser.parseComponentType(objectRoot, "cover_component", CoverRasterTileAccess.class);
            int flattenRange = valueParser.parseInteger(objectRoot, "flatten_range");
            CoverType waterCoverType = valueParser.parseRegistryEntry(objectRoot, "water_cover", CoverRegistry.getRegistry());
            return new WaterFlattenAdapter(heightComponent, coverComponent, flattenRange, waterCoverType);
        }
    }
}
