package net.gegy1000.earth.server.shared;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.gegy1000.earth.server.util.OpProgressWatcher;
import net.gegy1000.earth.server.util.ProcedureProgressWatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SharedDataInitializers {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setNameFormat("terrarium-shared-initializer")
                    .setDaemon(true)
                    .build()
    );

    private static final List<SharedDataInitializer> INITIALIZERS = new ArrayList<>();

    public static void add(SharedDataInitializer... initializers) {
        Collections.addAll(INITIALIZERS, initializers);
    }

    static CompletableFuture<SharedEarthData> initialize(ProcedureProgressWatcher watcher) {
        return CompletableFuture.supplyAsync(() -> {
            SharedEarthData data = new SharedEarthData();

            for (int index = 0; index < INITIALIZERS.size(); index++) {
                SharedDataInitializer initializer = INITIALIZERS.get(index);

                OpProgressWatcher opWatcher = watcher.startOp(initializer.getDescription());
                try {
                    initializer.initialize(data, opWatcher);
                    opWatcher.notifyComplete();
                } catch (SharedInitException e) {
                    opWatcher.notifyException(e);
                }

                watcher.notifyProgress((index + 1.0) / INITIALIZERS.size());
            }

            watcher.notifyComplete();

            return data;
        }, EXECUTOR);
    }
}
