package net.gegy1000.terrarium.server.world.data;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.gegy1000.justnow.future.Future;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public final class DataContext {
    public static final DataContext INSTANCE = new DataContext();

    private static final Executor BLOCKING_EXECUTOR = Executors.newFixedThreadPool(2,
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("terrarium-data-worker-%d")
                    .build()
    );

    private DataContext() {
    }

    public <T> Future<T> spawnBlocking(Supplier<T> supplier) {
        return Future.spawnBlocking(BLOCKING_EXECUTOR, supplier);
    }
}
