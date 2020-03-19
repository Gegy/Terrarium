package net.gegy1000.terrarium.server.world.data;

import com.google.common.collect.ImmutableMap;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.util.FutureUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class DataGenerator {
    private final ImmutableMap<DataKey<?>, DataOp<?>> attachedData;

    public DataGenerator(ImmutableMap<DataKey<?>, DataOp<?>> attachedData) {
        this.attachedData = attachedData;
    }

    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<Map<DataKey<?>, Optional<?>>> generateFuture(DataView view, Collection<DataKey<?>> keys) {
        ImmutableMap.Builder<DataKey<?>, CompletableFuture<Optional<?>>> futures = ImmutableMap.builder();
        for (DataKey<?> key : keys) {
            DataOp<?> op = this.attachedData.get(key);
            if (op != null) {
                CompletableFuture<Optional<?>> future = (CompletableFuture<Optional<?>>) (CompletableFuture) op.apply(view);
                futures.put(key, future);
            }
        }

        return FutureUtil.allOf(futures.build());
    }

    public ColumnData generateOnly(DataView view, Collection<DataKey<?>> keys) {
        CompletableFuture<Map<DataKey<?>, Optional<?>>> future = this.generateFuture(view, keys);

        Map<DataKey<?>, Optional<?>> result = new HashMap<>();
        try {
            result = future.join();
        } catch (Exception e) {
            Terrarium.LOGGER.error("Failed to load DataOp result", e);
        }

        for (DataKey<?> key : this.attachedData.keySet()) {
            if (!result.containsKey(key)) {
                result.put(key, Optional.empty());
            }
        }

        return new ColumnData(result);
    }

    public ColumnData generate(DataView view) {
        return this.generateOnly(view, this.attachedData.keySet());
    }

    public CompletableFuture<Void> preload(DataView view) {
        return this.generateFuture(view, this.attachedData.keySet())
                .thenApply(m -> null);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> generateOne(DataView view, DataKey<T> key) {
        DataOp<?> op = this.attachedData.get(key);
        if (op != null) {
            return (Optional<T>) op.apply(view).join();
        }
        return Optional.empty();
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
