package net.gegy1000.terrarium.server.world.data;

import com.google.common.collect.ImmutableMap;
import net.gegy1000.terrarium.Terrarium;

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

    public ColumnData generate(DataView view) {
        ImmutableMap.Builder<DataKey<?>, Optional<?>> result = ImmutableMap.builder();

        this.attachedData.forEach((key, op) -> {
            try {
                result.put(key, op.apply(view).join());
            } catch (Exception e) {
                Terrarium.LOGGER.error("Failed to load DataOp result", e);
                result.put(key, Optional.empty());
            }
        });

        return new ColumnData(result.build());
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
