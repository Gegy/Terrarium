package net.gegy1000.terrarium.server.world.pipeline;

import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;

public class DataLayer<T extends TiledDataAccess, P extends TiledDataAccess> extends CachedDataProducer<T> {
    private final DataLayerProcessor<T, P> processor;
    private final DataLayerProducer<P> parent;

    private DataLayer(DataLayerProcessor<T, P> processor, DataLayerProducer<P> parent) {
        this.processor = processor;
        this.parent = parent;
    }

    public static <T extends TiledDataAccess, P extends TiledDataAccess> DataLayerProducer<T> of(DataLayerProcessor<T, P> processor, DataLayerProducer<P> parent) {
        return new DataLayer<>(processor, parent);
    }

    @Override
    public void reset() {
        super.reset();
        this.parent.reset();
    }

    @Override
    public T create(DataView view) {
        DataView parentView = this.processor.getParentView(view);
        P parentResult = this.parent.apply(parentView);
        return this.processor.apply(view, parentResult, parentView);
    }
}
