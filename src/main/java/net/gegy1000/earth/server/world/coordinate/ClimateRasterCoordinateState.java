package net.gegy1000.earth.server.world.coordinate;

import net.gegy1000.earth.server.world.pipeline.source.WorldClimateDataset;
import net.gegy1000.terrarium.server.world.coordinate.ScaledCoordinateState;

public class ClimateRasterCoordinateState extends ScaledCoordinateState {
    private static final int OFFSET_X = WorldClimateDataset.WIDTH / 2;
    private static final int OFFSET_Y = WorldClimateDataset.HEIGHT / 2;

    public ClimateRasterCoordinateState(double scale) {
        super(scale);
    }

    @Override
    public double getBlockX(double x, double z) {
        return super.getBlockX(x - OFFSET_X, z - OFFSET_Y);
    }

    @Override
    public double getBlockZ(double x, double z) {
        return super.getBlockZ(x - OFFSET_X, z - OFFSET_Y);
    }

    @Override
    public double getX(double blockX, double blockZ) {
        return super.getX(blockX, blockZ) + OFFSET_X;
    }

    @Override
    public double getZ(double blockX, double blockZ) {
        return super.getZ(blockX, blockZ) + OFFSET_Y;
    }
}
