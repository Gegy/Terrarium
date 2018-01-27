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

    public static Coordinate fromBlock(EarthGenerationSettings settings, double blockX, double blockZ) {
        return new Coordinate(settings, blockX * settings.getInverseScale(), blockZ * settings.getInverseScale());
    }

    public static Coordinate fromGlob(EarthGenerationSettings settings, double globX, double globZ) {
        return new Coordinate(settings, globX / 0.3, globZ / 0.3);
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
        return this.globalX * 0.3;
    }

    public double getGlobZ() {
        return this.globalZ * 0.3;
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

    public Coordinate addBlock(double blockX, double blockZ) {
        double offsetX = blockX * this.settings.getInverseScale();
        double offsetZ = blockZ * this.settings.getInverseScale();
        return new Coordinate(this.settings, this.globalX + offsetX, this.globalZ + offsetZ);
    }

    public Coordinate addGlob(double globX, double globZ) {
        double offsetX = globX / 0.3;
        double offsetZ = globZ / 0.3;
        return new Coordinate(this.settings, this.globalX + offsetX, this.globalZ + offsetZ);
    }

    public Coordinate subtract(Coordinate coordinate) {
        return new Coordinate(this.settings, this.globalX - coordinate.globalX, this.globalZ - coordinate.globalZ);
    }

    public BlockPos toBlockPos() {
        return new BlockPos(this.getBlockX(), 0, this.getBlockZ());
    }

    public boolean inWorldBounds() {
        double latitude = this.getLatitude();
        double longitude = this.getLongitude();
        return latitude >= -90.0 && longitude >= -180.0 && latitude <= 90.0 && longitude <= 180.0;
    }
}
