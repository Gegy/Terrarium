package net.gegy1000.terrarium.server.world.coordinate;

public final class LngLatCoordinateState implements CoordinateState {
    private final double scale;

    public LngLatCoordinateState(double scale) {
        this.scale = scale;
    }

    @Override
    public double getBlockX(double latitude, double longitude) {
        return latitude * this.scale;
    }

    @Override
    public double getBlockZ(double latitude, double longitude) {
        return -longitude * this.scale;
    }

    @Override
    public double getX(double blockX, double blockZ) {
        return blockX / this.scale;
    }

    @Override
    public double getZ(double blockX, double blockZ) {
        return -blockZ / this.scale;
    }
}
