package net.gegy1000.terrarium.server.world.pipeline.data.op;

import net.gegy1000.terrarium.server.world.pipeline.data.DataView;

import java.util.concurrent.CompletableFuture;

public interface DataFunction<T> {
    CompletableFuture<T> apply(DataView view);
}
