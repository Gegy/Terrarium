package net.gegy1000.earth.server.world.geography;

public enum Landform {
    LAND,
    SEA,
    LAKE_OR_RIVER;

    public boolean isWater() {
        return this != LAND;
    }

    public boolean isLand() {
        return this == LAND;
    }
}
