package net.gegy1000.terrarium.server.world.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.gegy1000.terrarium.server.util.FutureUtil;
import net.gegy1000.terrarium.server.world.data.op.DataFunction;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class DataOp<T> {
    private final DataFunction<T> function;

    private Cache<DataView, CompletableFuture<Optional<T>>> cache;
    private Function<T, T> copy;

    private DataOp(DataFunction<T> function) {
        this.function = function;
    }

    public static <T> DataOp<T> of(DataFunction<T> function) {
        return new DataOp<>(function);
    }

    public static <T> DataOp<T> ofSync(Function<DataView, T> function) {
        return new DataOp<>(view -> {
            T result = function.apply(view);
            return CompletableFuture.completedFuture(Optional.of(result));
        });
    }

    public static <T> DataOp<T> completed(Optional<T> result) {
        return new DataOp<>(view -> CompletableFuture.completedFuture(result));
    }

    public DataOp<T> cached(Function<T, T> copy) {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(4)
                .expireAfterAccess(5, TimeUnit.SECONDS)
                .build();
        this.copy = copy;
        return this;
    }

    public CompletableFuture<Optional<T>> apply(DataView view) {
        if (this.cache == null) return this.function.apply(view);

        try {
            return this.cache.get(view, () -> this.function.apply(view))
                    .thenApply(opt -> opt.map(v -> this.copy.apply(v)));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public <U> DataOp<U> map(BiFunction<T, DataView, U> map) {
        return DataOp.of(view -> {
            CompletableFuture<Optional<T>> future = this.apply(view);
            return future.thenApply(opt -> opt.map(result -> map.apply(result, view)));
        });
    }

    public <U> DataOp<U> flatMap(BiFunction<T, DataView, Optional<U>> map) {
        return DataOp.of(view -> {
            CompletableFuture<Optional<T>> future = this.apply(view);
            return future.thenApply(opt -> opt.flatMap(result -> map.apply(result, view)));
        });
    }

    public static <A, B, R> DataOp<R> map2(DataOp<A> a, DataOp<B> b, Map2<A, B, R> map) {
        return DataOp.of(view -> {
            return FutureUtil.map2(a.apply(view), b.apply(view), (aOption, bOption) -> {
                if (aOption.isPresent() && bOption.isPresent()) {
                    return Optional.of(map.apply(view, aOption.get(), bOption.get()));
                }
                return Optional.empty();
            });
        });
    }

    public static <A, B, C, R> DataOp<R> map3(DataOp<A> a, DataOp<B> b, DataOp<C> c, Map3<A, B, C, R> map) {
        return DataOp.of(view -> {
            return FutureUtil.map3(a.apply(view), b.apply(view), c.apply(view), (aOption, bOption, cOption) -> {
                if (aOption.isPresent() && bOption.isPresent() && cOption.isPresent()) {
                    return Optional.of(map.apply(view, aOption.get(), bOption.get(), cOption.get()));
                }
                return Optional.empty();
            });
        });
    }

    public interface Map2<A, B, R> {
        R apply(DataView view, A a, B b);
    }

    public interface Map3<A, B, C, R> {
        R apply(DataView view, A a, B b, C c);
    }
}
