package net.gegy1000.earth.server.world.pipeline.adapter;

import net.gegy1000.earth.server.world.cover.EarthCoverTypes;
import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRaster;
import net.gegy1000.terrarium.server.util.FloodFill;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.TerrariumCoverTypes;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.CoverRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.region.RegionData;

import java.util.LinkedList;
import java.util.List;

public class WaterApplyAdapter implements RegionAdapter {
    protected final CoordinateState geoCoordinateState;
    protected final RegionComponentType<WaterRaster> waterComponent;
    protected final RegionComponentType<ShortRaster> heightComponent;
    protected final RegionComponentType<CoverRaster> coverComponent;

    public WaterApplyAdapter(CoordinateState geoCoordinateState, RegionComponentType<WaterRaster> waterComponent, RegionComponentType<ShortRaster> heightComponent, RegionComponentType<CoverRaster> coverComponent) {
        this.geoCoordinateState = geoCoordinateState;
        this.waterComponent = waterComponent;
        this.heightComponent = heightComponent;
        this.coverComponent = coverComponent;
    }

    @Override
    public void adapt(RegionData data, int x, int z, int width, int height) {
        short[] heightBuffer = data.getOrExcept(this.heightComponent).getShortData();
        CoverType[] coverBuffer = data.getOrExcept(this.coverComponent).getData();
        WaterRaster waterTile = data.getOrExcept(this.waterComponent);

        List<FloodFill.Point> unselectedPoints = new LinkedList<>();
        for (int localY = 0; localY < height; localY++) {
            for (int localX = 0; localX < width; localX++) {
                int index = localX + localY * width;
                int sampleType = waterTile.getWaterType(localX, localY);
                if (WaterRaster.isWater(sampleType)) {
                    coverBuffer[index] = EarthCoverTypes.WATER;
                } else {
                    CoverType<?> currentCover = coverBuffer[index];
                    if (currentCover == EarthCoverTypes.WATER) {
                        coverBuffer[index] = TerrariumCoverTypes.PLACEHOLDER;
                        unselectedPoints.add(new FloodFill.Point(localX, localY));
                        heightBuffer[index] = (short) Math.max(1, heightBuffer[index]);
                    }
                }
            }
        }

        for (FloodFill.Point point : unselectedPoints) {
            CoverSelectVisitor visitor = new CoverSelectVisitor();
            FloodFill.floodVisit(coverBuffer, width, height, point, visitor);
            coverBuffer[point.getX() + point.getY() * width] = visitor.getResult();
        }
    }

    protected class CoverSelectVisitor implements FloodFill.Visitor<CoverType> {
        protected CoverType result = null;

        @Override
        public CoverType visit(FloodFill.Point point, CoverType sampled) {
            if (sampled != TerrariumCoverTypes.PLACEHOLDER) {
                this.result = sampled;
                return null;
            }
            return sampled;
        }

        @Override
        public boolean canVisit(FloodFill.Point point, CoverType sampled) {
            return sampled != EarthCoverTypes.WATER;
        }

        public CoverType getResult() {
            if (this.result == null) {
                return EarthCoverTypes.RAINFED_CROPS;
            }
            return this.result;
        }
    }
}
