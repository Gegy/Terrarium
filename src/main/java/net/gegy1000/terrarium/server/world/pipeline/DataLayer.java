package net.gegy1000.terrarium.server.world.pipeline;

import net.gegy1000.terrarium.server.world.pipeline.layer.LayerContext;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;

import java.util.Collection;

public interface DataLayer<T extends TiledDataAccess> {
    T apply(LayerContext context, DataView view);

    Collection<DataTileKey<?>> getRequiredData(LayerContext context, DataView view);
}
