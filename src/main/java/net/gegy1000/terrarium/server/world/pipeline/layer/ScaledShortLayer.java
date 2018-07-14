package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;

public class ScaledShortLayer extends InterpolatingScaleLayer<ShortRasterTile> {
    public ScaledShortLayer(DataLayer<ShortRasterTile> parent, CoordinateState coordinateState, Interpolation.Method interpolationMethod) {
        super(parent, interpolationMethod, coordinateState);
    }

    @Override
    protected ShortRasterTile apply(ShortRasterTile parent, DataView view, DataView parentView, double scaleFactorX, double scaleFactorY, double originOffsetX, double originOffsetY) {
        ShortRasterTile resultHeights = new ShortRasterTile(view);
        this.scaleRegion(parent, resultHeights, scaleFactorX, scaleFactorY, originOffsetX, originOffsetY);
        return resultHeights;
    }
}
