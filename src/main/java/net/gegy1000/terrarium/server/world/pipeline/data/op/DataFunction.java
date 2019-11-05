package net.gegy1000.terrarium.server.world.pipeline.data.op;

import net.gegy1000.terrarium.server.world.pipeline.data.DataView;
import net.gegy1000.terrarium.server.world.pipeline.data.Data;

import java.util.concurrent.CompletableFuture;

public interface DataFunction<T extends Data> {
    CompletableFuture<T> apply(DataView view);
}
