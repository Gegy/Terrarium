package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.DataSourceHandler;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;

public interface LayerContext {
    <T extends TiledDataAccess> T apply(DataLayer<T> layer, DataView view);

    DataSourceHandler getSourceHandler();
}
