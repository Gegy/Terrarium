package net.gegy1000.terrarium.server.world.pipeline.source.tile;

public interface MergableTile<T extends MergableTile<T>> extends TiledDataAccess {
    T merge(T other);
}
