package futures.future;

import com.google.common.base.Functions;
import futures.Waker;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Future<T> {
    static <T> BlockingTaskHandle<T> spawnBlocking(Executor executor, Supplier<T> supplier) {
        BlockingTaskHandle<T> handle = new BlockingTaskHandle<>();
        executor.execute(() -> {
            handle.setExecutingThread(Thread.currentThread());
            T result = supplier.get();
            handle.complete(result);
        });
        return handle;
    }

    static <T> Future<T> ready(T value) {
        return new Ready<>(value);
    }

    static <T> Future<T> pending() {
        return new Pending<>();
    }

    static <T> Cancelable<T> cancelable(Future<T> future) {
        return new Cancelable<>(future);
    }

    static <T> JoinAll<T> joinAll(Collection<Future<T>> futures) {
        return new JoinAll<>(futures);
    }

    static <T> JoinAll<T> joinAll(Stream<Future<T>> futures) {
        return new JoinAll<>(futures.collect(Collectors.toCollection(LinkedList::new)));
    }

    static <K, V> Future<Map<K, V>> joinAll(Map<K, Future<V>> map) {
        return new JoinAllMap<>(map);
    }

    static <A, B, R> Future<R> map2(Future<A> a, Future<B> b, BiFunction<A, B, R> map) {
        return new Map2<>(a, b, map);
    }

    static <A, B, R> Future<R> andThen2(Future<A> a, Future<B> b, BiFunction<A, B, Future<R>> andThen) {
        return new Map2<>(a, b, andThen).andThen(Functions.identity());
    }

    @Nullable
    T poll(Waker waker);

    default <U> Future<U> map(Function<T, U> map) {
        return new Map1<>(this, map);
    }

    default <U> Future<U> andThen(Function<T, Future<U>> andThen) {
        return new AndThen<>(this, andThen);
    }

    default <U> Future<U> handle(BiFunction<T, Throwable, U> handle) {
        return new Handle<>(this, handle);
    }
}
