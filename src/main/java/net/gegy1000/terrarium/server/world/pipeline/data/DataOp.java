package net.gegy1000.terrarium.server.world.pipeline.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.gegy1000.terrarium.server.world.pipeline.data.op.DataFunction;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class DataOp<T> {
    private final DataFunction<T> function;

    private Cache<DataView, CompletableFuture<T>> cache;
    private Function<T, T> copy;

    private DataOp(DataFunction<T> function) {
        this.function = function;
    }

    public static <T> DataOp<T> of(DataFunction<T> function) {
        return new DataOp<>(function);
    }

    public static <T> DataOp<T> completed(T value) {
        return new DataOp<>(view -> CompletableFuture.completedFuture(value));
    }

    public DataOp<T> cached(Function<T, T> copy) {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(4)
                .expireAfterAccess(5, TimeUnit.SECONDS)
                .build();
        this.copy = copy;
        return this;
    }

    public CompletableFuture<T> apply(DataView view) {
        if (this.cache == null) return this.function.apply(view);

        try {
            return this.cache.get(view, () -> this.function.apply(view)).thenApply(this.copy);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public <U> DataOp<U> map(BiFunction<T, DataView, U> mapper) {
        return DataOp.of(view -> {
            CompletableFuture<T> future = this.apply(view);
            return future.thenApply(data -> mapper.apply(data, view));
        });
    }
}

