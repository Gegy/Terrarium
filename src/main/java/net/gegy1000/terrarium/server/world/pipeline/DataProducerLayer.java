package net.gegy1000.terrarium.server.world.pipeline;

import net.gegy1000.terrarium.server.world.pipeline.layer.LayerContext;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;

import java.util.Collection;
import java.util.Collections;

public interface DataProducerLayer<T extends TiledDataAccess> extends DataLayer<T> {
    @Override
    default Collection<DataTileKey<?>> getRequiredData(LayerContext context, DataView view) {
        return Collections.emptyList();
    }
}
