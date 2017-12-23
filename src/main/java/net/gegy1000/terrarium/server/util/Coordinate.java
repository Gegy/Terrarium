package net.gegy1000.terrarium.server.util;

import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.BlockPos;

public class Coordinate {
    private final EarthGenerationSettings settings;
    private final double globalX;
    private final double globalZ;

    public Coordinate(EarthGenerationSettings settings, double globalX, double globalZ) {
        this.settings = settings;
        this.globalX = globalX;
        this.globalZ = globalZ;
    }

    public static Coordinate fromLatLng(EarthGenerationSettings settings, double latitude, double longitude) {
        return new Coordinate(settings, longitude * 1200.0, -latitude * 1200.0);
    }

    public double getGlobalX() {
        return this.globalX;
    }

    public double getGlobalZ() {
        return this.globalZ;
    }

    public double getLatitude() {
        return -this.globalZ / 1200.0;
    }

    public double getLongitude() {
        return this.globalX / 1200.0;
    }

    public double getGlobX() {
        return this.globalX * 3.0 / 10.0;
    }

    public double getGlobZ() {
        return this.globalZ * 3.0 / 10.0;
    }

    public double getBlockX() {
        return this.globalX * this.settings.getFinalScale();
    }

    public double getBlockZ() {
        return this.globalZ * this.settings.getFinalScale();
    }

    public Coordinate add(double globalX, double globalZ) {
        return new Coordinate(this.settings, this.globalX + globalX, this.globalZ + globalZ);
    }

    public Coordinate add(Coordinate coordinate) {
        return new Coordinate(this.settings, this.globalX + coordinate.globalX, this.globalZ + coordinate.globalZ);
    }

    public Coordinate subtract(Coordinate coordinate) {
        return new Coordinate(this.settings, this.globalX - coordinate.globalX, this.globalZ - coordinate.globalZ);
    }

    public BlockPos toBlockPos(int y) {
        return new BlockPos(this.getBlockX(), y, this.getBlockZ());
    }
}
