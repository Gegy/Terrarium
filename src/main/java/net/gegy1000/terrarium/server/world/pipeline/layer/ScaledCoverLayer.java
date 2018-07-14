package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.util.Voronoi;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;

public class ScaledCoverLayer extends BufferedScalingLayer<CoverRasterTile> {
    private final Voronoi voronoi;

    public ScaledCoverLayer(DataLayer<CoverRasterTile> parent, CoordinateState coordinateState) {
        super(parent, 1, 1, coordinateState);
        this.voronoi = new Voronoi(Voronoi.DistanceFunc.EUCLIDEAN, 0.9, 4, 1000);
    }

    @Override
    protected CoverRasterTile apply(CoverRasterTile parent, DataView view, DataView parentView, double scaleFactorX, double scaleFactorY, double originOffsetX, double originOffsetY) {
        CoverRasterTile result = new CoverRasterTile(view);
        this.voronoi.scale(parent.getData(), result.getData(), parentView, view, scaleFactorX, scaleFactorY, originOffsetX, originOffsetY);
        return result;
    }

    @Override
    public DataView getParentView(DataView view) {
        return super.getParentView(view).grow(0, 0, 1, 1);
    }
}
