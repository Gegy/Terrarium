package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.ParentedDataLayer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;
import net.minecraft.util.math.MathHelper;

public abstract class BufferedScalingLayer<T extends TiledDataAccess> extends ParentedDataLayer<T, T> {
    protected final int lowerSampleBuffer;
    protected final int upperSampleBuffer;

    private final CoordinateState scaledState;

    protected BufferedScalingLayer(DataLayer<T> parent, int lowerSampleBuffer, int upperSampleBuffer, CoordinateState scaledState) {
        super(parent);
        this.lowerSampleBuffer = lowerSampleBuffer;
        this.upperSampleBuffer = upperSampleBuffer;

        this.scaledState = scaledState;
    }

    protected abstract T apply(T parent, DataView view, DataView parentView, double scaleFactorX, double scaleFactorY, double originOffsetX, double originOffsetY);

    @Override
    protected final T apply(LayerContext context, DataView view, T parent, DataView parentView) {
        double blockSizeX = view.getWidth();
        double blockSizeY = view.getHeight();

        double scaleFactorX = this.scaledState.getX(blockSizeX, blockSizeY) / blockSizeX;
        double scaleFactorY = this.scaledState.getZ(blockSizeX, blockSizeY) / blockSizeY;

        Coordinate minRegionCoordinateBlock = view.getMinCoordinate().to(this.scaledState);
        Coordinate maxRegionCoordinateBlock = view.getMaxCoordinate().to(this.scaledState);

        Coordinate minRegionCoordinate = Coordinate.min(minRegionCoordinateBlock, maxRegionCoordinateBlock);

        double originOffsetX = minRegionCoordinate.getX() - parentView.getX();
        double originOffsetZ = minRegionCoordinate.getZ() - parentView.getY();

        return this.apply(parent, view, parentView, scaleFactorX, scaleFactorY, originOffsetX, originOffsetZ);
    }

    @Override
    public DataView getParentView(DataView view) {
        Coordinate minRegionCoordinateBlock = view.getMinCoordinate().to(this.scaledState);
        Coordinate maxRegionCoordinateBlock = view.getMaxCoordinate().to(this.scaledState);

        Coordinate minRegionCoordinate = Coordinate.min(minRegionCoordinateBlock, maxRegionCoordinateBlock);
        Coordinate maxRegionCoordinate = Coordinate.max(minRegionCoordinateBlock, maxRegionCoordinateBlock);

        int minSampleX = MathHelper.floor(minRegionCoordinate.getX()) - this.lowerSampleBuffer;
        int minSampleY = MathHelper.floor(minRegionCoordinate.getZ()) - this.lowerSampleBuffer;

        int maxSampleX = MathHelper.ceil(maxRegionCoordinate.getX()) + this.upperSampleBuffer;
        int maxSampleY = MathHelper.ceil(maxRegionCoordinate.getZ()) + this.upperSampleBuffer;

        return new DataView(minSampleX, minSampleY, maxSampleX - minSampleX, maxSampleY - minSampleY);
    }
}
