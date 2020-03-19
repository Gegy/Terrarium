package net.gegy1000.terrarium.server.world.coordinate;

public final class CoordinateReference {
    private static final CoordinateReference BLOCK = new CoordinateReference(1.0, 1.0);

    private final double scaleX;
    private final double scaleZ;

    private CoordinateReference(double scaleX, double scaleZ) {
        this.scaleX = scaleX;
        this.scaleZ = scaleZ;
    }

    public static CoordinateReference block() {
        return BLOCK;
    }

    public static CoordinateReference scale(double x, double z) {
        return new CoordinateReference(x, z);
    }

    public static CoordinateReference scale(double scale) {
        return new CoordinateReference(scale, scale);
    }

    public static CoordinateReference lngLat(double scale) {
        return new CoordinateReference(scale, -scale);
    }

    public double blockX(double x) {
        return x * this.scaleX;
    }

    public double blockZ(double z) {
        return z * this.scaleZ;
    }

    public double x(double blockX) {
        return blockX / this.scaleX;
    }

    public double z(double blockZ) {
        return blockZ / this.scaleZ;
    }

    public double scaleX() {
        return this.scaleX;
    }

    public double scaleZ() {
        return this.scaleZ;
    }

    public Coordinate coord(double x, double z) {
        return new Coordinate(this, x, z);
    }
}
