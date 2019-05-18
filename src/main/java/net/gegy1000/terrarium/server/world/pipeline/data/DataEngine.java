package net.gegy1000.terrarium.server.world.pipeline.data;

import com.google.common.collect.ImmutableMap;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.util.FutureUtil;
import net.gegy1000.terrarium.server.world.pipeline.source.DataSourceHandler;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class DataEngine {
    private final ImmutableMap<DataKey<?>, DataOp<?>> attachedData;

    private DataEngine(ImmutableMap<DataKey<?>, DataOp<?>> attachedData) {
        this.attachedData = attachedData;
    }

    public static Builder builder() {
        return new Builder();
    }

    public <T extends Data> CompletableFuture<T> load(DataOp<T> future, DataView view) {
        return future.apply(this, view);
    }

    // TODO: adapters
    private CompletableFuture<Optional<? extends Data>> tryLoad(DataOp<?> future, DataView view) {
        return future.apply(this, view)
                .handle((data, throwable) -> {
                    if (throwable != null) {
                        Terrarium.LOGGER.error("Failed to load result", throwable);
                        return Optional.empty();
                    }
                    return Optional.of(data);
                });
    }

    public ColumnData populateData(ChunkPos columnPos) {
        DataView view = DataView.of(columnPos);

        ImmutableMap.Builder<DataKey<?>, Optional<? extends Data>> result = ImmutableMap.builder();

        Collection<CompletableFuture<Void>> futures = new ArrayList<>(this.attachedData.size());
        this.attachedData.forEach((key, future) -> {
            CompletableFuture<Optional<? extends Data>> loaded = this.tryLoad(future, view);
            futures.add(loaded.thenAccept(o -> result.put(key, o)));
        });

        FutureUtil.joinAll(futures);

        return new ColumnData(result.build());
    }

    public DataSourceHandler getSourceHandler() {
        return DataSourceHandler.INSTANCE;
    }

    public static class Builder {
        private final ImmutableMap.Builder<DataKey<?>, DataOp<?>> attachedData = ImmutableMap.builder();

        private Builder() {
        }

        public <T extends Data> Builder with(DataKey<T> key, DataOp<T> data) {
            this.attachedData.put(key, data);
            return this;
        }

        public DataEngine build() {
            return new DataEngine(this.attachedData.build());
        }
    }
}
