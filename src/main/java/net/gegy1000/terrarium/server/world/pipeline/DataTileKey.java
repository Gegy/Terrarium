package net.gegy1000.terrarium.server.world.pipeline;

import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;

public class DataTileKey<T extends TiledDataAccess> {
    private final TiledDataSource<T> source;
    private final int tileX;
    private final int tileZ;

    public DataTileKey(TiledDataSource<T> source, int tileX, int tileZ) {
        this.source = source;
        this.tileX = tileX;
        this.tileZ = tileZ;
    }

    public DataTilePos toPos() {
        return new DataTilePos(this.tileX, this.tileZ);
    }

    public TiledDataSource<T> getSource() {
        return this.source;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o instanceof DataTileKey) {
            DataTileKey key = (DataTileKey) o;
            return key.source == this.source && key.tileX == this.tileX && key.tileZ == this.tileZ;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = this.source.getIdentifier().hashCode();
        result = 31 * result + this.tileX;
        result = 31 * result + this.tileZ;
        return result;
    }

    @Override
    public String toString() {
        return "DataTileKey{" + "source=" + this.source + ", tileX=" + this.tileX + ", tileZ=" + this.tileZ + '}';
    }
}
