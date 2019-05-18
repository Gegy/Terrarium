/*
package net.gegy1000.earth.server.world.pipeline.adapter;

import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRaster;
import net.gegy1000.terrarium.server.util.FloodFill;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.adapter.ColumnAdapter;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnData;
import net.gegy1000.terrarium.server.world.pipeline.data.DataKey;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ObjRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

// TODO: Problematic
public class WaterApplyAdapter implements ColumnAdapter {
    protected final CoordinateState geoCoordinateState;
    protected final DataKey<WaterRaster> waterKey;
    protected final DataKey<ShortRaster> heightKey;
    protected final DataKey<ObjRaster<Cover>> coverKey;

    public WaterApplyAdapter(CoordinateState geoCoordinateState, DataKey<WaterRaster> waterKey, DataKey<ShortRaster> heightKey, DataKey<ObjRaster<Cover>> coverKey) {
        this.geoCoordinateState = geoCoordinateState;
        this.waterKey = waterKey;
        this.heightKey = heightKey;
        this.coverKey = coverKey;
    }

    @Override
    public void apply(ColumnData data, int x, int z, int width, int height) {
        Optional<ShortRaster> heightRaster = data.get(this.heightKey);
        Optional<ObjRaster<Cover>> coverRaster = data.get(this.coverKey);
        Optional<WaterRaster> waterRaster = data.get(this.waterKey);

        if (heightRaster.isPresent() && coverRaster.isPresent() && waterRaster.isPresent()) {
            short[] heightBuffer = heightRaster.get().getData();
            Cover[] coverBuffer = coverRaster.get().getData();
            WaterRaster waterTile = waterRaster.get();

            List<FloodFill.Point> unselectedPoints = new LinkedList<>();
            for (int localY = 0; localY < height; localY++) {
                for (int localX = 0; localX < width; localX++) {
                    int index = localX + localY * width;
                    int sampleType = waterTile.getWaterType(localX, localY);
                    if (WaterRaster.isWater(sampleType)) {
                        coverBuffer[index] = Cover.WATER;
                    } else {
                        Cover currentCover = coverBuffer[index];
                        if (currentCover == Cover.WATER) {
                            coverBuffer[index] = Cover.NO_DATA;
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
    }

    protected class CoverSelectVisitor implements FloodFill.Visitor<Cover> {
        protected Cover result = null;

        @Override
        public Cover visit(FloodFill.Point point, Cover sampled) {
            if (sampled != Cover.NO_DATA) {
                this.result = sampled;
                return null;
            }
            return sampled;
        }

        @Override
        public boolean canVisit(FloodFill.Point point, Cover sampled) {
            return sampled != Cover.WATER;
        }

        public Cover getResult() {
            if (this.result == null) {
                return Cover.URBAN;
            }
            return this.result;
        }
    }
}
*/
