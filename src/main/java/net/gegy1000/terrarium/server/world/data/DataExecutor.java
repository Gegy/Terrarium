package net.gegy1000.terrarium.server.world.data;

import futures.future.Future;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

public interface DataExecutor extends Executor {
    default <T> Future<T> spawnBlocking(Supplier<T> supplier) {
        return Future.spawnBlocking(this, supplier);
    }
}
