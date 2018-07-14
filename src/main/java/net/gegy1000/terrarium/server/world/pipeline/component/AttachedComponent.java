package net.gegy1000.terrarium.server.world.pipeline.component;

import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataTileKey;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.layer.LayerContext;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;

import java.util.Collection;

public final class AttachedComponent<T extends TiledDataAccess> {
    private final RegionComponentType<T> type;
    private final DataLayer<T> layer;

    public AttachedComponent(RegionComponentType<T> type, DataLayer<T> layer) {
        this.type = type;
        this.layer = layer;
    }

    public RegionComponentType<T> getType() {
        return this.type;
    }

    public RegionComponent<T> createAndPopulate(LayerContext context, DataView view) {
        return new RegionComponent<>(this.type, context.apply(this.layer, view));
    }

    public Collection<DataTileKey<?>> getRequiredData(LayerContext context, DataView view) {
        return this.layer.getRequiredData(context, view);
    }
}
