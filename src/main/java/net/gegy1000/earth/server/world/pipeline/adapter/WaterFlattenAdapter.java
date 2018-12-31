package net.gegy1000.earth.server.world.pipeline.adapter;

import net.gegy1000.terrarium.server.util.FloodFill;
import net.gegy1000.terrarium.server.world.cover.TerrariumCover;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.BiomeRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class WaterFlattenAdapter implements RegionAdapter {
    private final RegionComponentType<ShortRasterTile> heightComponent;
    private final RegionComponentType<BiomeRasterTile> biomeComponent;
    private final int flattenRange;

    private final Biome waterBiome;

    public WaterFlattenAdapter(RegionComponentType<ShortRasterTile> heightComponent, RegionComponentType<BiomeRasterTile> biomeComponent, int flattenRange, Biome waterBiome) {
        this.heightComponent = heightComponent;
        this.biomeComponent = biomeComponent;
        this.flattenRange = flattenRange;
        this.waterBiome = waterBiome;
    }

    @Override
    public void adapt(RegionData data, int x, int z, int width, int height) {
        ShortRasterTile heightTile = data.getOrExcept(this.heightComponent);
        BiomeRasterTile coverTile = data.getOrExcept(this.biomeComponent);

        short[] heightBuffer = heightTile.getShortData();
        Biome[] coverBuffer = coverTile.getData();

        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                if (coverBuffer[localX + localZ * width] == this.waterBiome) {
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
            if (coverBuffer[i] == TerrariumCover.NONE) {
                coverBuffer[i] = this.waterBiome;
            }
        }
    }

    private void flattenArea(List<FloodFill.Point> waterPoints, short targetHeight, short[] heightBuffer, Biome[] coverBuffer, int width, int height) {
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

    private boolean hasNeighbouringLand(int x, int z, Biome[] coverBuffer, int width, int height) {
        int index = x + z * width;
        return (x > 0 && coverBuffer[index - 1] != TerrariumCover.NONE)
                || (x < width - 1 && coverBuffer[index + 1] != TerrariumCover.NONE)
                || (z > 0 && coverBuffer[index - width] != TerrariumCover.NONE)
                || (z < height - 1 && coverBuffer[index + width] != TerrariumCover.NONE);
    }

    private class AverageCoverHeightVisitor implements FloodFill.Visitor<Biome> {
        private final short[] heightBuffer;
        private final int width;

        private final List<FloodFill.Point> visitedPoints = new LinkedList<>();

        private long totalHeight;

        private AverageCoverHeightVisitor(short[] heightBuffer, int width) {
            this.heightBuffer = heightBuffer;
            this.width = width;
        }

        @Override
        public Biome visit(FloodFill.Point point, Biome sampled) {
            this.totalHeight += this.heightBuffer[point.getX() + point.getY() * this.width];
            this.visitedPoints.add(point);
            return TerrariumCover.NONE;
        }

        @Override
        public boolean canVisit(FloodFill.Point point, Biome sampled) {
            return sampled == WaterFlattenAdapter.this.waterBiome;
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
