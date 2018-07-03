package net.gegy1000.terrarium.server.world.coordinate;

public class LatLngCoordinateState implements CoordinateState {
    private final double scale;

    public LatLngCoordinateState(double scale) {
        this.scale = scale;
    }

    @Override
    public final double getBlockX(double latitude, double longitude) {
        return longitude * this.scale;
    }

    @Override
    public final double getBlockZ(double latitude, double longitude) {
        return -latitude * this.scale;
    }

    @Override
    public final double getX(double blockX, double blockZ) {
        return -blockZ / this.scale;
    }

    @Override
    public final double getZ(double blockX, double blockZ) {
        return blockX / this.scale;
    }
}
