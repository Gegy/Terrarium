package net.gegy1000.terrarium.server.world.pipeline.data.function;

import net.gegy1000.terrarium.server.world.pipeline.data.DataView;
import net.gegy1000.terrarium.server.world.pipeline.data.Data;
import net.gegy1000.terrarium.server.world.pipeline.data.DataEngine;

import java.util.concurrent.CompletableFuture;

public interface DataFunction<T extends Data> {
    CompletableFuture<T> apply(DataEngine engine, DataView view);
}
