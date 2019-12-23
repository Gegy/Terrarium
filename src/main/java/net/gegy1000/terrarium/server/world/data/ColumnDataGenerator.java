package net.gegy1000.terrarium.server.world.data;

import com.google.common.collect.ImmutableMap;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ColumnDataGenerator {
    private final ImmutableMap<DataKey<?>, DataOp<?>> attachedData;

    public ColumnDataGenerator(ImmutableMap<DataKey<?>, DataOp<?>> attachedData) {
        this.attachedData = attachedData;
    }

    public static Builder builder() {
        return new Builder();
    }

    public ColumnData generate(ChunkPos columnPos) {
        DataView view = DataView.of(columnPos);

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

        public ColumnDataGenerator build() {
            return new ColumnDataGenerator(ImmutableMap.copyOf(this.attachedData));
        }
    }
}
