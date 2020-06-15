package net.gegy1000.terrarium.server.world.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.gegy1000.justnow.future.Future;
import net.gegy1000.justnow.future.MaybeDone;
import net.gegy1000.terrarium.server.world.data.op.DataFunction;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class DataOp<T> implements DataFunction<T> {
    private final DataFunction<T> function;

    private Cache<DataView, MaybeDone<Optional<T>>> cache;
    private Function<T, T> copy;

    private DataOp(DataFunction<T> function) {
        this.function = function;
    }

    public static <T> DataOp<T> of(DataFunction<T> function) {
        return new DataOp<>(function);
    }

    public static <T> DataOp<T> ofLazy(Function<DataView, T> function) {
        return new DataOp<>((view, ctx) -> Future.lazy(() -> Optional.of(function.apply(view))));
    }

    public static <T> DataOp<T> ofBlocking(Function<DataView, T> function) {
        return new DataOp<>((view, ctx) -> {
            return ctx.spawnBlocking(() -> {
                T result = function.apply(view);
                return Optional.of(result);
            });
        });
    }

    public static <T> DataOp<T> ready(Optional<T> result) {
        return new DataOp<>((view, ctx) -> Future.ready(result));
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
    public Future<Optional<T>> apply(DataView view, DataContext ctx) {
        if (this.cache == null) return this.function.apply(view, ctx);

        try {
            MaybeDone<Optional<T>> maybeDone = this.cache.get(view, () -> {
                return Future.maybeDone(this.function.apply(view, ctx));
            });
            return maybeDone.map(u -> {
                if (!maybeDone.isDone()) return Optional.empty();
                return maybeDone.getResult().map(this.copy);
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public <U> DataOp<U> map(BiFunction<T, DataView, U> map) {
        return DataOp.of((view, ctx) -> {
            Future<Optional<T>> future = this.apply(view, ctx);
            return future.map(opt -> {
                return opt.map(result -> map.apply(result, view));
            });
        });
    }

    public <U> DataOp<U> mapBlocking(BiFunction<T, DataView, U> map) {
        return DataOp.of((view, ctx) -> {
            Future<Optional<T>> future = this.apply(view, ctx);
            return future.andThen(opt -> {
                return ctx.spawnBlocking(() -> {
                    return opt.map(result -> map.apply(result, view));
                });
            });
        });
    }

    public static <A, B, R> DataOp<R> map2(DataOp<A> a, DataOp<B> b, Map2<A, B, R> map) {
        return DataOp.of((view, ctx) -> {
            return Future.map2(a.apply(view, ctx), b.apply(view, ctx), (aOption, bOption) -> {
                if (aOption.isPresent() && bOption.isPresent()) {
                    return Optional.of(map.apply(view, aOption.get(), bOption.get()));
                }
                return Optional.empty();
            });
        });
    }

    public interface Map2<A, B, R> {
        R apply(DataView view, A a, B b);
    }
}
