package net.gegy1000.terrarium.server.world.pipeline;

import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;

public class DataSource<T extends TiledDataAccess> extends CachedDataProducer<T> {
    private final DataLayerProducer<T> producer;

    private DataSource(DataLayerProducer<T> producer) {
        this.producer = producer;
    }

    public static <T extends TiledDataAccess> DataSource<T> from(DataLayerProducer<T> producer) {
        return new DataSource<>(producer);
    }

    @Override
    public void reset() {
        super.reset();
        this.producer.reset();
    }

    @Override
    public T create(DataView view) {
        return this.producer.apply(view);
    }
}
