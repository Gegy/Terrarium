package net.gegy1000.terrarium.server.world.pipeline.data;

import net.gegy1000.terrarium.server.world.pipeline.source.DataSourceHandler;

import java.util.concurrent.CompletableFuture;

public final class DataEngine {
    public <T extends Data> CompletableFuture<T> load(DataOp<T> op, DataView view) {
        return op.apply(this, view);
    }

    public DataSourceHandler getSourceHandler() {
        return DataSourceHandler.INSTANCE;
    }
}
