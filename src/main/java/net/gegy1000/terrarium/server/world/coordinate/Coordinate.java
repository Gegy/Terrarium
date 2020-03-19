package net.gegy1000.terrarium.server.world.coordinate;

import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public final class Coordinate {
    private final CoordinateReference crs;

    private final double x;
    private final double z;

    public Coordinate(CoordinateReference crs, double x, double z) {
        this.crs = crs;
        this.x = x;
        this.z = z;
    }

    public static Coordinate atBlock(double blockX, double blockZ) {
        return new Coordinate(null, blockX, blockZ);
    }

    public double getX() {
        return this.x;
    }

    public double getZ() {
        return this.z;
    }

    public double getBlockX() {
        if (this.crs == null) {
            return this.x;
        }
        return this.crs.blockX(this.x);
    }

    public double getBlockZ() {
        if (this.crs == null) {
            return this.z;
        }
        return this.crs.blockZ(this.z);
    }

    public Coordinate to(CoordinateReference to) {
        if (this.crs == to) {
            return this;
        }

        double blockX = this.getBlockX();
        double blockZ = this.getBlockZ();
        return new Coordinate(to, to.x(blockX), to.z(blockZ));
    }

    public Coordinate addBlock(double x, double z) {
        return this.add(Coordinate.atBlock(x, z));
    }

    public Coordinate add(CoordinateReference state, double x, double z) {
        return this.add(new Coordinate(state, x, z));
    }

    public Coordinate add(Coordinate coordinate) {
        double blockX = coordinate.getBlockX();
        double blockZ = coordinate.getBlockZ();

        if (this.crs == null) {
            return Coordinate.atBlock(this.x + blockX, this.z + blockZ);
        }

        double offsetX = this.crs.x(blockX);
        double offsetZ = this.crs.z(blockX);
        return new Coordinate(this.crs, this.x + offsetX, this.z + offsetZ);
    }

    public Coordinate toBlock() {
        return new Coordinate(null, this.getBlockX(), this.getBlockZ());
    }

    public BlockPos toBlockPos() {
        return new BlockPos(this.getBlockX(), 0, this.getBlockZ());
    }

    public boolean is(CoordinateReference state) {
        return Objects.equals(this.crs, state);
    }

    public static Coordinate min(Coordinate left, Coordinate right) {
        if (!left.is(right.crs)) {
            throw new IllegalArgumentException("Cannot get minimum coordinate between coordinates of different state");
        }
        return new Coordinate(left.crs, Math.min(left.getX(), right.getX()), Math.min(left.getZ(), right.getZ()));
    }

    public static Coordinate max(Coordinate left, Coordinate right) {
        if (!left.is(right.crs)) {
            throw new IllegalArgumentException("Cannot get maximum coordinate between coordinates of different state");
        }
        return new Coordinate(left.crs, Math.max(left.getX(), right.getX()), Math.max(left.getZ(), right.getZ()));
    }

    @Override
    public String toString() {
        return String.format("(%.4f; %.4f)", this.x, this.z);
    }
}
