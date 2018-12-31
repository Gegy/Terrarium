package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.util.Voronoi;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.BiomeRasterTile;

public class ScaledCoverLayer extends BufferedScalingLayer<BiomeRasterTile> {
    private final Voronoi voronoi;

    public ScaledCoverLayer(DataLayer<BiomeRasterTile> parent, CoordinateState coordinateState) {
        super(parent, 1, 1, coordinateState);
        this.voronoi = new Voronoi(Voronoi.DistanceFunc.EUCLIDEAN, 0.9, 4, 1000);
    }

    @Override
    protected BiomeRasterTile apply(BiomeRasterTile parent, DataView view, DataView parentView, double scaleFactorX, double scaleFactorY, double originOffsetX, double originOffsetY) {
        BiomeRasterTile result = new BiomeRasterTile(view);
        this.voronoi.scale(parent.getData(), result.getData(), parentView, view, scaleFactorX, scaleFactorY, originOffsetX, originOffsetY);
        return result;
    }

    @Override
    public DataView getParentView(DataView view) {
        return super.getParentView(view).grow(0, 0, 1, 1);
    }
}
