package net.gegy1000.terrarium.server.world.pipeline.component;

import net.gegy1000.terrarium.server.world.pipeline.DataLayerProducer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;
import net.gegy1000.terrarium.server.world.region.RegionTilePos;

public final class AttachedComponent<T extends TiledDataAccess> {
    private final RegionComponentType<T> type;
    private final DataLayerProducer<T> producer;

    public AttachedComponent(RegionComponentType<T> type, DataLayerProducer<T> producer) {
        this.type = type;
        this.producer = producer;
    }

    public RegionComponentType<T> getType() {
        return this.type;
    }

    public RegionComponent<T> createAndPopulate(RegionTilePos pos, int width, int height) {
        this.producer.reset();
        DataView view = new DataView(pos.getMinBufferedX(), pos.getMinBufferedZ(), width, height);
        return new RegionComponent<>(this.type, this.producer.apply(view));
    }
}
