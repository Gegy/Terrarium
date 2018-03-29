package net.gegy1000.terrarium.server.world.coordinate;

public class ScaledCoordinateState implements CoordinateState {
    private final double scaleX;
    private final double scaleZ;

    public ScaledCoordinateState(double scaleX, double scaleZ) {
        this.scaleX = scaleX;
        this.scaleZ = scaleZ;
    }

    public ScaledCoordinateState(double scale) {
        this(scale, scale);
    }

    @Override
    public final double getBlockX(double x, double z) {
        return x * this.scaleX;
    }

    @Override
    public final double getBlockZ(double x, double z) {
        return z * this.scaleZ;
    }

    @Override
    public final double getX(double blockX, double blockZ) {
        return blockX / this.scaleX;
    }

    @Override
    public final double getZ(double blockX, double blockZ) {
        return blockZ / this.scaleZ;
    }
}
