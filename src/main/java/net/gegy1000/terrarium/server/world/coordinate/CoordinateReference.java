package net.gegy1000.terrarium.server.world.coordinate;

public final class CoordinateReference {
    private static final CoordinateReference BLOCK = new CoordinateReference(1.0, 1.0, 0.0, 0.0);

    private double scaleX;
    private double scaleZ;

    private double offsetX;
    private double offsetZ;

    private CoordinateReference(double scaleX, double scaleZ, double offsetX, double offsetZ) {
        this.scaleX = scaleX;
        this.scaleZ = scaleZ;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
    }

    public static CoordinateReference block() {
        return BLOCK;
    }

    public static CoordinateReference scaleAndOffset(
            double scaleX, double scaleZ,
            double offsetX, double offsetZ
    ) {
        return new CoordinateReference(scaleX, scaleZ, offsetX, offsetZ);
    }

    public static CoordinateReference scale(double scale) {
        return new CoordinateReference(scale, scale, 0.0, 0.0);
    }

    public static CoordinateReference lngLat(double scale) {
        return new CoordinateReference(scale, -scale, 0.0, 0.0);
    }

    public double blockX(double x) {
        return (x + this.offsetX) * this.scaleX;
    }

    public double blockZ(double z) {
        return (z + this.offsetZ) * this.scaleZ;
    }

    public double x(double blockX) {
        return (blockX / this.scaleX) - this.offsetX;
    }

    public double z(double blockZ) {
        return (blockZ / this.scaleZ) - this.offsetZ;
    }

    public double scaleX() {
        return this.scaleX;
    }

    public double scaleZ() {
        return this.scaleZ;
    }

    public double avgScale() {
        return (this.scaleX + this.scaleZ) / 2.0;
    }

    public Coordinate coord(double x, double z) {
        return new Coordinate(this, x, z);
    }
}
