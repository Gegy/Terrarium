package net.gegy1000.terrarium.server.world.pipeline.component;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.pipeline.data.DataView;
import net.gegy1000.terrarium.server.world.pipeline.data.Data;
import net.gegy1000.terrarium.server.world.pipeline.data.DataEngine;
import net.gegy1000.terrarium.server.world.pipeline.data.DataFuture;

import java.util.concurrent.CompletableFuture;

public final class AttachedComponent<T extends Data> {
    private final RegionComponentType<T> type;
    private final DataFuture<T> future;

    public AttachedComponent(RegionComponentType<T> type, DataFuture<T> future) {
        this.type = type;
        this.future = future;
    }

    public RegionComponentType<T> getType() {
        return this.type;
    }

    public CompletableFuture<RegionComponent<?>> createAndPopulate(DataEngine engine, DataView view) {
        return engine.load(this.future, view)
                .thenApply(data -> new RegionComponent<>(this.type, data))
                .handle((component, throwable) -> {
                    if (throwable != null) {
                        Terrarium.LOGGER.error("Failed to load component {}", this.type.getIdentifier(), throwable);
                        return this.type.createDefaultComponent(view.getWidth(), view.getHeight());
                    }
                    return component;
                });
    }
}
