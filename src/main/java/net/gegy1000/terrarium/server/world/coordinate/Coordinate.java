package net.gegy1000.terrarium.server.world.coordinate;

import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class Coordinate {
    private final CoordinateState state;

    private final double x;
    private final double z;

    public Coordinate(CoordinateState state, double x, double z) {
        this.state = state;
        this.x = x;
        this.z = z;
    }

    public static Coordinate fromBlock(double blockX, double blockZ) {
        return new Coordinate(null, blockX, blockZ);
    }

    public double getX() {
        return this.x;
    }

    public double getZ() {
        return this.z;
    }

    public double getBlockX() {
        if (this.state == null) {
            return this.x;
        }
        return this.state.getBlockX(this.x, this.z);
    }

    public double getBlockZ() {
        if (this.state == null) {
            return this.z;
        }
        return this.state.getBlockZ(this.x, this.z);
    }

    public Coordinate to(CoordinateState to) {
        if (this.state == to) {
            return this;
        }

        double blockX = this.getBlockX();
        double blockZ = this.getBlockZ();
        return new Coordinate(to, to.getX(blockX, blockZ), to.getZ(blockX, blockZ));
    }

    public Coordinate addBlock(double x, double z) {
        return this.add(Coordinate.fromBlock(x, z));
    }

    public Coordinate add(CoordinateState state, double x, double z) {
        return this.add(new Coordinate(state, x, z));
    }

    public Coordinate add(Coordinate coordinate) {
        double blockX = coordinate.getBlockX();
        double blockZ = coordinate.getBlockZ();

        if (this.state == null) {
            return Coordinate.fromBlock(this.x + blockX, this.z + blockZ);
        }

        double offsetX = this.state.getX(blockX, blockZ);
        double offsetZ = this.state.getZ(blockX, blockZ);
        return new Coordinate(this.state, this.x + offsetX, this.z + offsetZ);
    }

    public BlockPos toBlockPos() {
        return new BlockPos(this.getBlockX(), 0, this.getBlockZ());
    }

    public boolean is(CoordinateState state) {
        return Objects.equals(this.state, state);
    }

    public static Coordinate min(Coordinate left, Coordinate right) {
        if (!left.is(right.state)) {
            throw new IllegalArgumentException("Cannot get minimum coordinate between coordinates of different state");
        }
        return new Coordinate(left.state, Math.min(left.getX(), right.getX()), Math.min(left.getZ(), right.getZ()));
    }

    public static Coordinate max(Coordinate left, Coordinate right) {
        if (!left.is(right.state)) {
            throw new IllegalArgumentException("Cannot get maximum coordinate between coordinates of different state");
        }
        return new Coordinate(left.state, Math.max(left.getX(), right.getX()), Math.max(left.getZ(), right.getZ()));
    }
}
