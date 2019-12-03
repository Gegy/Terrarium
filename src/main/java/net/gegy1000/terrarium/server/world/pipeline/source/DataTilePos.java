package net.gegy1000.terrarium.server.world.pipeline.source;

public class DataTilePos {
    protected final int x;
    protected final int z;

    public DataTilePos(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    @Override
    public String toString() {
        return "DataTilePos{x=" + this.x + ", z=" + this.z + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DataTilePos) {
            DataTilePos pos = (DataTilePos) obj;
            return this.x == pos.x && this.z == pos.z;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = this.x;
        result = 31 * result + this.z;
        return result;
    }

    public static DataTilePos min(DataTilePos left, DataTilePos right) {
        return new DataTilePos(Math.min(left.getX(), right.getX()), Math.min(left.getZ(), right.getZ()));
    }

    public static DataTilePos max(DataTilePos left, DataTilePos right) {
        return new DataTilePos(Math.max(left.getX(), right.getX()), Math.max(left.getZ(), right.getZ()));
    }
}
