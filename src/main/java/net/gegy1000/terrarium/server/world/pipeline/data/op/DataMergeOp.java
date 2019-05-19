package net.gegy1000.terrarium.server.world.pipeline.data.op;

import net.gegy1000.terrarium.server.util.FutureUtil;
import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
import net.gegy1000.terrarium.server.world.pipeline.data.MergableData;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class DataMergeOp {
    @SafeVarargs
    public static <T extends MergableData<T>> DataOp<T> merge(DataOp<T>... futures) {
        return merge(Arrays.asList(futures));
    }

    public static <T extends MergableData<T>> DataOp<T> merge(Collection<DataOp<T>> futures) {
        if (futures.isEmpty()) {
            throw new IllegalArgumentException("Cannot merge no data");
        }

        return DataOp.of((engine, view) -> {
            CompletableFuture<Collection<T>> dataFutures = FutureUtil.allOf(
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
