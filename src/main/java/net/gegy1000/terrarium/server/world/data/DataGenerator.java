package net.gegy1000.terrarium.server.world.data;

import com.google.common.collect.ImmutableMap;
import net.gegy1000.justnow.future.Future;
import net.gegy1000.terrarium.Terrarium;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class DataGenerator {
    private final ImmutableMap<DataKey<?>, DataOp<?>> attachedData;

    public DataGenerator(ImmutableMap<DataKey<?>, DataOp<?>> attachedData) {
        this.attachedData = attachedData;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Future<ColumnData> generate(DataView view, Collection<DataKey<?>> keys) {
        ImmutableMap.Builder<DataKey<?>, Future<Optional<?>>> futures = ImmutableMap.builder();
        for (DataKey<?> key : keys) {
            DataOp<?> op = this.attachedData.get(key);
            if (op != null) {
                Future<Optional<?>> future = op.apply(view, DataContext.INSTANCE).handle((result, throwable) -> {
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
                    DataStore<Optional<?>> store = new DataStore<>();
                    for (DataKey<?> key : keys) {
                        Optional<?> value = result.getOrDefault(key, Optional.empty());
                        store.put(key, value);
                    }
                    return new ColumnData(view, store);
                });
    }

    public Future<ColumnData> generate(DataView view) {
        return this.generate(view, this.attachedData.keySet());
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
