package net.gegy1000.terrarium.server.world.pipeline.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.gegy1000.terrarium.server.world.pipeline.data.op.DataFunction;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public final class DataOp<T extends Data> {
    private final DataFunction<T> function;
    private final Cache<DataView, CompletableFuture<T>> cache = CacheBuilder.newBuilder()
            .maximumSize(4)
            .expireAfterAccess(5, TimeUnit.SECONDS)
            .build();

    private DataOp(DataFunction<T> function) {
        this.function = function;
    }

    public static <T extends Data> DataOp<T> of(DataFunction<T> function) {
        return new DataOp<>(function);
    }

    public static <T extends Data> DataOp<T> completed(T value) {
        return new DataOp<>(view -> CompletableFuture.completedFuture(value));
    }

    public CompletableFuture<T> apply(DataView view) {
        try {
            return this.cache.get(view, () -> this.function.apply(view));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public <U extends Data> DataOp<U> map(Mapper<T, U> mapper) {
        return DataOp.of(view -> {
            CompletableFuture<T> future = this.apply(view);
            return future.thenApply(data -> mapper.map(data, view));
        });
    }

    public interface Mapper<T extends Data, U extends Data> {
        U map(T data, DataView view);
    }
}

