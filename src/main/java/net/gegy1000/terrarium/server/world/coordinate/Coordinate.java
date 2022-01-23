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

    public double x() {
        return this.x;
    }

    public double z() {
        return this.z;
    }

    public double blockX() {
        if (this.crs == null) {
            return this.x;
        }
        return this.crs.blockX(this.x);
    }

    public double blockZ() {
        if (this.crs == null) {
            return this.z;
        }
        return this.crs.blockZ(this.z);
    }

    public Coordinate to(CoordinateReference to) {
        if (this.crs == to) {
            return this;
        }

        double blockX = this.blockX();
        double blockZ = this.blockZ();
        return new Coordinate(to, to.x(blockX), to.z(blockZ));
    }

    public Coordinate addBlock(double x, double z) {
        return this.add(Coordinate.atBlock(x, z));
    }

    public Coordinate addLocal(double x, double z) {
        return new Coordinate(this.crs, this.x + x, this.z + z);
    }

    public Coordinate add(CoordinateReference state, double x, double z) {
        return this.add(new Coordinate(state, x, z));
    }

    public Coordinate add(Coordinate coordinate) {
        double blockX = coordinate.blockX();
        double blockZ = coordinate.blockZ();

        if (this.crs == null) {
            return Coordinate.atBlock(this.x + blockX, this.z + blockZ);
        }

        double offsetX = this.crs.x(blockX);
        double offsetZ = this.crs.z(blockX);
        return new Coordinate(this.crs, this.x + offsetX, this.z + offsetZ);
    }

    public Coordinate toBlock() {
        return new Coordinate(null, this.blockX(), this.blockZ());
    }

    public BlockPos toBlockPos() {
        return new BlockPos(this.blockX(), 0, this.blockZ());
    }

    public boolean is(CoordinateReference state) {
        return Objects.equals(this.crs, state);
    }

    public static Coordinate min(Coordinate left, Coordinate right) {
        if (!left.is(right.crs)) {
            throw new IllegalArgumentException("Cannot get minimum coordinate between coordinates of different state");
        }
        return new Coordinate(left.crs, Math.min(left.x(), right.x()), Math.min(left.z(), right.z()));
    }

    public static Coordinate max(Coordinate left, Coordinate right) {
        if (!left.is(right.crs)) {
            throw new IllegalArgumentException("Cannot get maximum coordinate between coordinates of different state");
        }
        return new Coordinate(left.crs, Math.max(left.x(), right.x()), Math.max(left.z(), right.z()));
    }

    @Override
    public String toString() {
        return String.format("(%.4f; %.4f)", this.x, this.z);
    }
}
