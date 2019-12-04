package net.gegy1000.terrarium.server.world.coordinate;

public class ScaledCoordRef implements CoordinateReference {
    private final double scaleX;
    private final double scaleZ;

    public ScaledCoordRef(double scaleX, double scaleZ) {
        this.scaleX = scaleX;
        this.scaleZ = scaleZ;
    }

    public ScaledCoordRef(double scale) {
        this(scale, scale);
    }

    @Override
    public double blockX(double x, double z) {
        return x * this.scaleX;
    }

    @Override
    public double blockZ(double x, double z) {
        return z * this.scaleZ;
    }

    @Override
    public double x(double blockX, double blockZ) {
        return blockX / this.scaleX;
    }

    @Override
    public double z(double blockX, double blockZ) {
        return blockZ / this.scaleZ;
    }
}
