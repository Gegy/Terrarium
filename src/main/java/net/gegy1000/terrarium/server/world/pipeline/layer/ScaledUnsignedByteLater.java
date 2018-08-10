package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.UnsignedByteRasterTile;

public class ScaledUnsignedByteLater extends InterpolatingScaleLayer<UnsignedByteRasterTile> {
    public ScaledUnsignedByteLater(DataLayer<UnsignedByteRasterTile> parent, CoordinateState coordinateState, Interpolation.Method interpolationMethod) {
        super(parent, interpolationMethod, coordinateState);
    }

    @Override
    protected UnsignedByteRasterTile apply(UnsignedByteRasterTile parent, DataView view, DataView parentView, double scaleFactorX, double scaleFactorY, double originOffsetX, double originOffsetY) {
        UnsignedByteRasterTile resultHeights = new UnsignedByteRasterTile(view);
        this.scaleRegion(parent, resultHeights, scaleFactorX, scaleFactorY, originOffsetX, originOffsetY);
        return resultHeights;
    }
}
