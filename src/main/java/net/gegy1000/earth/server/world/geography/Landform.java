package net.gegy1000.earth.server.world.geography;

public enum Landform {
    LAND,
    BEACH,
    SEA,
    LAKE_OR_RIVER;

    public boolean isWater() {
        return this == SEA || this == LAKE_OR_RIVER;
    }

    public boolean isLand() {
        return this == LAND || this == BEACH;
    }
}
