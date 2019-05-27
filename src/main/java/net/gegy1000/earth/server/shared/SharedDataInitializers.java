package net.gegy1000.earth.server.shared;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.gegy1000.earth.server.util.ProcessTracker;
import net.gegy1000.earth.server.util.ProgressTracker;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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

    public static CompletableFuture<SharedEarthData> initialize(ProcessTracker tracker) {
        ProgressTracker master = tracker.push(new TextComponentString("Initializing"), INITIALIZERS.size());

        return CompletableFuture.supplyAsync(() -> {
            try {
                SharedEarthData data = new SharedEarthData();

                for (SharedDataInitializer initializer : INITIALIZERS) {
                    initializer.initialize(data, tracker);
                    master.step(1);

                    if (tracker.isErrored()) {
                        throw new CompletionException(tracker.getException());
                    }
                }

                master.markComplete();
                tracker.markComplete();

                return data;
            } catch (Throwable t) {
                t.printStackTrace();
                throw t;
            }
        }, EXECUTOR);
    }
}
