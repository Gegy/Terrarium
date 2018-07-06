package net.gegy1000.terrarium.server.world.pipeline;

import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;

public interface DataLayerProducer<T extends TiledDataAccess> {
    default void reset() {
    }

    T apply(DataView view);
}
