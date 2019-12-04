package net.gegy1000.terrarium.server.world.coordinate;

public final class LatLngCoordRef implements CoordinateReference {
    private final double scale;

    public LatLngCoordRef(double scale) {
        this.scale = scale;
    }

    @Override
    public double blockX(double latitude, double longitude) {
        return longitude * this.scale;
    }

    @Override
    public double blockZ(double latitude, double longitude) {
        return -latitude * this.scale;
    }

    @Override
    public double x(double blockX, double blockZ) {
        return -blockZ / this.scale;
    }

    @Override
    public double z(double blockX, double blockZ) {
        return blockX / this.scale;
    }
}
