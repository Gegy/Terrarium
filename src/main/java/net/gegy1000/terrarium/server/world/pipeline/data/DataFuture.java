package net.gegy1000.terrarium.server.world.pipeline.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.data.function.DataFunction;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public final class DataFuture<T extends Data> {
    private final DataFunction<T> function;
    private final Cache<DataView, CompletableFuture<T>> cache = CacheBuilder.newBuilder()
            .maximumSize(4)
            .expireAfterAccess(5, TimeUnit.SECONDS)
            .build();

    private DataFuture(DataFunction<T> function) {
        this.function = function;
    }

    public static <T extends Data> DataFuture<T> of(DataFunction<T> function) {
        return new DataFuture<>(function);
    }

    public static <T extends Data> DataFuture<T> completed(T value) {
        return new DataFuture<>((engine, view) -> CompletableFuture.completedFuture(value));
    }

    CompletableFuture<T> apply(DataEngine engine, DataView view) {
        try {
            return this.cache.get(view, () -> this.function.apply(engine, view));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}

