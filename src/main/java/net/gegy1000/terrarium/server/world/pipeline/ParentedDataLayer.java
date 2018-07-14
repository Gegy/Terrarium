package net.gegy1000.terrarium.server.world.pipeline;

import net.gegy1000.terrarium.server.world.pipeline.layer.LayerContext;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;

import java.util.Collection;

public abstract class ParentedDataLayer<T extends TiledDataAccess, P extends TiledDataAccess> implements DataLayer<T> {
    private final DataLayer<P> parent;

    protected ParentedDataLayer(DataLayer<P> parent) {
        this.parent = parent;
    }

    protected abstract DataView getParentView(DataView view);

    protected abstract T apply(LayerContext context, DataView view, P parent, DataView parentView);

    @Override
    public final T apply(LayerContext context, DataView view) {
        DataView parentView = this.getParentView(view);
        P parentData = context.apply(this.parent, parentView);
        return this.apply(context, view, parentData, parentView);
    }

    @Override
    public Collection<DataTileKey<?>> getRequiredData(LayerContext context, DataView view) {
        return this.parent.getRequiredData(context, this.getParentView(view));
    }
}
