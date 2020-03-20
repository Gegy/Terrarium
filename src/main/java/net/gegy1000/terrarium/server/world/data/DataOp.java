package net.gegy1000.terrarium.server.world.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.gegy1000.justnow.future.Future;
import net.gegy1000.terrarium.server.world.data.op.DataFunction;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class DataOp<T> implements DataFunction<T> {
    private final DataFunction<T> function;

    private Cache<DataView, Future<Optional<T>>> cache;
    private Function<T, T> copy;

    private DataOp(DataFunction<T> function) {
        this.function = function;
    }

    public static <T> DataOp<T> of(DataFunction<T> function) {
        return new DataOp<>(function);
    }

    public static <T> DataOp<T> ofBlocking(Function<DataView, T> function) {
        return new DataOp<>((view, executor) -> {
            return executor.spawnBlocking(() -> {
                T result = function.apply(view);
                return Optional.of(result);
            });
        });
    }

    public static <T> DataOp<T> ready(Optional<T> result) {
        return new DataOp<>((view, executor) -> Future.ready(result));
    }

    public DataOp<T> cached(Function<T, T> copy) {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(4)
                .expireAfterAccess(5, TimeUnit.SECONDS)
                .build();
        this.copy = copy;
        return this;
    }

    @Override
    public Future<Optional<T>> apply(DataView view, DataExecutor executor) {
        if (this.cache == null) return this.function.apply(view, executor);

        try {
            return this.cache.get(view, () -> this.function.apply(view, executor))
                    .map(opt -> opt.map(v -> this.copy.apply(v)));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public <U> DataOp<U> map(BiFunction<T, DataView, U> map) {
        return DataOp.of((view, executor) -> {
            Future<Optional<T>> future = this.apply(view, executor);
            return future.andThen(opt -> {
                return executor.spawnBlocking(() -> {
                    return opt.map(result -> map.apply(result, view));
                });
            });
        });
    }

    public <U> DataOp<U> flatMap(BiFunction<T, DataView, Optional<U>> map) {
        return DataOp.of((view, executor) -> {
            Future<Optional<T>> future = this.apply(view, executor);
            return future.andThen(opt -> {
                return executor.spawnBlocking(() -> {
                    return opt.flatMap(result -> map.apply(result, view));
                });
            });
        });
    }

    public static <A, B, R> DataOp<R> map2(DataOp<A> a, DataOp<B> b, Map2<A, B, R> map) {
        return DataOp.of((view, executor) -> {
            return Future.andThen2(a.apply(view, executor), b.apply(view, executor), (aOption, bOption) -> {
                return executor.spawnBlocking(() -> {
                    if (aOption.isPresent() && bOption.isPresent()) {
                        return Optional.of(map.apply(view, aOption.get(), bOption.get()));
                    }
                    return Optional.empty();
                });
            });
        });
    }

    public interface Map2<A, B, R> {
        R apply(DataView view, A a, B b);
    }
}
