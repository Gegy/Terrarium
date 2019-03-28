package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.FloatRasterTile;

public class ScaledFloatLayer extends InterpolatingScaleLayer<FloatRasterTile> {
    public ScaledFloatLayer(DataLayer<FloatRasterTile> parent, CoordinateState coordinateState, Interpolation.Method interpolationMethod) {
        super(parent, interpolationMethod, coordinateState);
    }

    @Override
    protected FloatRasterTile apply(FloatRasterTile parent, DataView view, DataView parentView, double scaleFactorX, double scaleFactorY, double originOffsetX, double originOffsetY) {
        FloatRasterTile resultTile = new FloatRasterTile(view);
        this.scaleRegion(parent, resultTile, scaleFactorX, scaleFactorY, originOffsetX, originOffsetY);
        return resultTile;
    }
}
