package net.gegy1000.terrarium.server.world.coordinate;

public interface CoordinateState {
    String DEFAULT_KEY = "default";

    double getBlockX(double x, double z);

    double getBlockZ(double x, double z);

    double getX(double blockX, double blockZ);

    double getZ(double blockX, double blockZ);
}
