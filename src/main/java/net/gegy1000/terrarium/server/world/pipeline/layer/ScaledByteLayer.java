package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTile;

public class ScaledByteLayer extends InterpolatingScaleLayer<ByteRasterTile> {
    public ScaledByteLayer(CoordinateState coordinateState, Interpolation.Method interpolationMethod) {
        super(interpolationMethod, coordinateState);
    }

    @Override
    protected ByteRasterTile apply(ByteRasterTile parent, DataView view, DataView parentView, double scaleFactorX, double scaleFactorY, double originOffsetX, double originOffsetY) {
        ByteRasterTile resultHeights = new ByteRasterTile(view);
        this.scaleRegion(parent, resultHeights, scaleFactorX, scaleFactorY, originOffsetX, originOffsetY);
        return resultHeights;
    }
}
