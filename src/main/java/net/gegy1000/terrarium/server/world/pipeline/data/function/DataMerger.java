package net.gegy1000.terrarium.server.world.pipeline.data.function;

import net.gegy1000.terrarium.server.util.FutureUtil;
import net.gegy1000.terrarium.server.world.pipeline.data.DataFuture;
import net.gegy1000.terrarium.server.world.pipeline.data.MergableData;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class DataMerger {
    public static <T extends MergableData<T>> DataFuture<T> merge(DataFuture<T>... futures) {
        return merge(Arrays.asList(futures));
    }

    public static <T extends MergableData<T>> DataFuture<T> merge(Collection<DataFuture<T>> futures) {
        if (futures.isEmpty()) {
            throw new IllegalArgumentException("Cannot merge no data");
        }

        return DataFuture.of((engine, view) -> {
            CompletableFuture<Collection<T>> dataFutures = FutureUtil.joinAll(
                    futures.stream()
                            .map(f -> engine.load(f, view))
                            .collect(Collectors.toList())
            );

            return dataFutures.thenApply(collection -> {
                Iterator<T> iterator = collection.iterator();

                T result = iterator.next();
                while (iterator.hasNext()) {
                    result = result.merge(iterator.next());
                }

                return result;
            });
        });
    }
}
