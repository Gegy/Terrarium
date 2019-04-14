package net.gegy1000.earth.server.world.pipeline.adapter;

import net.gegy1000.earth.server.world.cover.CoverClassification;
import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRaster;
import net.gegy1000.terrarium.server.util.FloodFill;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.earth.server.world.pipeline.source.tile.CoverRaster;
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
        CoverClassification[] coverBuffer = data.getOrExcept(this.coverComponent).getData();
        WaterRaster waterTile = data.getOrExcept(this.waterComponent);

        List<FloodFill.Point> unselectedPoints = new LinkedList<>();
        for (int localY = 0; localY < height; localY++) {
            for (int localX = 0; localX < width; localX++) {
                int index = localX + localY * width;
                int sampleType = waterTile.getWaterType(localX, localY);
                if (WaterRaster.isWater(sampleType)) {
                    coverBuffer[index] = CoverClassification.WATER;
                } else {
                    CoverClassification currentCover = coverBuffer[index];
                    if (currentCover == CoverClassification.WATER) {
                        coverBuffer[index] = CoverClassification.NO_DATA;
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

    protected class CoverSelectVisitor implements FloodFill.Visitor<CoverClassification> {
        protected CoverClassification result = null;

        @Override
        public CoverClassification visit(FloodFill.Point point, CoverClassification sampled) {
            if (sampled != CoverClassification.NO_DATA) {
                this.result = sampled;
                return null;
            }
            return sampled;
        }

        @Override
        public boolean canVisit(FloodFill.Point point, CoverClassification sampled) {
            return sampled != CoverClassification.WATER;
        }

        public CoverClassification getResult() {
            if (this.result == null) {
                return CoverClassification.URBAN;
            }
            return this.result;
        }
    }
}
