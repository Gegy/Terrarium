package net.gegy1000.terrarium.server.world.pipeline.data;

import com.google.common.collect.ImmutableMap;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.util.math.ChunkPos;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
                CompletableFuture<?> future = op.apply(view);
                result.put(key, Optional.of(future.join()));
            } catch (Exception e) {
                Terrarium.LOGGER.error("Failed to load DataOp result", e);
                result.put(key, Optional.empty());
            }
        });

        return new ColumnData(result.build());
    }

    public static class Builder {
        private final ImmutableMap.Builder<DataKey<?>, DataOp<?>> attachedData = ImmutableMap.builder();

        private Builder() {
        }

        public <T> Builder with(DataKey<T> key, DataOp<T> data) {
            this.attachedData.put(key, data);
            return this;
        }

        public ColumnDataGenerator build() {
            return new ColumnDataGenerator(this.attachedData.build());
        }
    }
}
