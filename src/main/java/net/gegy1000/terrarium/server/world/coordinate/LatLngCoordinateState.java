package net.gegy1000.terrarium.server.world.coordinate;

public class LatLngCoordinateState implements CoordinateState {
    private final double finalScale;

    protected LatLngCoordinateState(double worldScale, double scaleMultiplier) {
        this.finalScale = worldScale * scaleMultiplier;
    }

    @Override
    public final double getBlockX(double latitude, double longitude) {
        return longitude * this.finalScale;
    }

    @Override
    public final double getBlockZ(double latitude, double longitude) {
        return -latitude * this.finalScale;
    }

    @Override
    public final double getX(double blockX, double blockZ) {
        return -blockZ / this.finalScale;
    }

    @Override
    public final double getZ(double blockX, double blockZ) {
        return blockX / this.finalScale;
    }
}
