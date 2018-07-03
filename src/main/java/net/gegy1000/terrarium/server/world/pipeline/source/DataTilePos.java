package net.gegy1000.terrarium.server.world.pipeline.source;

public class DataTilePos {
    private final int tileX;
    private final int tileZ;

    public DataTilePos(int tileX, int tileZ) {
        this.tileX = tileX;
        this.tileZ = tileZ;
    }

    public int getTileX() {
        return this.tileX;
    }

    public int getTileZ() {
        return this.tileZ;
    }

    @Override
    public String toString() {
        return "DataTilePos{tileX=" + this.tileX + ", tileZ=" + this.tileZ + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DataTilePos) {
            DataTilePos pos = (DataTilePos) obj;
            return this.tileX == pos.tileX && this.tileZ == pos.tileZ;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = this.tileX;
        result = 31 * result + this.tileZ;
        return result;
    }

    public static DataTilePos min(DataTilePos left, DataTilePos right) {
        return new DataTilePos(Math.min(left.getTileX(), right.getTileX()), Math.min(left.getTileZ(), right.getTileZ()));
    }

    public static DataTilePos max(DataTilePos left, DataTilePos right) {
        return new DataTilePos(Math.max(left.getTileX(), right.getTileX()), Math.max(left.getTileZ(), right.getTileZ()));
    }
}
