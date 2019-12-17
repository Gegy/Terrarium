package net.gegy1000.terrarium.server.world.data.op;

import net.gegy1000.terrarium.server.world.data.DataView;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface DataFunction<T> {
    CompletableFuture<Optional<T>> apply(DataView view);
}
