package net.gegy1000.terrarium.server.world.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.gegy1000.justnow.future.Future;
import net.gegy1000.terrarium.Terrarium;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class DataGenerator {
    public static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("terrarium-data-worker")
                    .build()
    );

    private static final DataExecutor DATA_EXECUTOR = EXECUTOR::execute;

    private final ImmutableMap<DataKey<?>, DataOp<?>> attachedData;

    public DataGenerator(ImmutableMap<DataKey<?>, DataOp<?>> attachedData) {
        this.attachedData = attachedData;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Future<ColumnData> generateOnly(DataView view, Collection<DataKey<?>> keys) {
        ImmutableMap.Builder<DataKey<?>, Future<Optional<?>>> futures = ImmutableMap.builder();
        for (DataKey<?> key : keys) {
            DataOp<?> op = this.attachedData.get(key);
            if (op != null) {
                Future<Optional<?>> future = op.apply(view, DATA_EXECUTOR).handle((result, throwable) -> {
                    if (throwable != null) {
                        Terrarium.LOGGER.error("Failed to load DataOp result", throwable);
                        return Optional.empty();
                    }
                    return result;
                });

                futures.put(key, future);
            }
        }

        return Future.joinAll(futures.build())
                .map(result -> {
                    for (DataKey<?> key : keys) {
                        if (!result.containsKey(key)) {
                            result.put(key, Optional.empty());
                        }
                    }
                    return new ColumnData(view, result);
                });
    }

    public Future<ColumnData> generate(DataView view) {
        return this.generateOnly(view, this.attachedData.keySet());
    }

    @SuppressWarnings("unchecked")
    public <T> Future<Optional<T>> generateOne(DataView view, DataKey<T> key) {
        DataOp<?> op = this.attachedData.get(key);
        if (op != null) {
            return op.apply(view, DATA_EXECUTOR).map(o -> (Optional<T>) o);
        }
        return Future.ready(Optional.empty());
    }

    public static class Builder {
        private final Map<DataKey<?>, DataOp<?>> attachedData = new HashMap<>();

        private Builder() {
        }

        public <T> Builder put(DataKey<T> key, DataOp<T> data) {
            this.attachedData.put(key, data);
            return this;
        }

        @SuppressWarnings("unchecked")
        public <T> DataOp<T> get(DataKey<T> key) {
            return (DataOp<T>) this.attachedData.get(key);
        }

        @SuppressWarnings("unchecked")
        public <T> DataOp<T> remove(DataKey<T> key) {
            return (DataOp<T>) this.attachedData.remove(key);
        }

        public DataGenerator build() {
            return new DataGenerator(ImmutableMap.copyOf(this.attachedData));
        }
    }
}
