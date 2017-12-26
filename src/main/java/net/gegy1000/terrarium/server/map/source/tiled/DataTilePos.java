package net.gegy1000.terrarium.server.map.source.tiled;

public class DataTilePos {
    private final int tileX;
    private final int tileY;

    public DataTilePos(int tileX, int tileY) {
        this.tileX = tileX;
        this.tileY = tileY;
    }

    public int getTileX() {
        return this.tileX;
    }

    public int getTileY() {
        return this.tileY;
    }

    @Override
    public String toString() {
        return "DataTilePos{tileX=" + this.tileX + ", tileY=" + this.tileY + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DataTilePos) {
            DataTilePos pos = (DataTilePos) obj;
            return this.tileX == pos.tileX && this.tileY == pos.tileY;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = this.tileX;
        result = 31 * result + this.tileY;
        return result;
    }
}
