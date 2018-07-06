package net.gegy1000.terrarium.server.world.pipeline;

import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;

public interface DataLayerProcessor<T extends TiledDataAccess, P extends TiledDataAccess> {
    T apply(DataView view, P parent, DataView parentView);

    DataView getParentView(DataView view);
}
