package net.gegy1000.terrarium.server.world.pipeline.component;

import net.gegy1000.terrarium.server.world.pipeline.data.Data;

public class RegionComponent<T extends Data> {
    private final RegionComponentType<T> type;
    private final T data;

    public RegionComponent(RegionComponentType<T> type, T data) {
        this.type = type;
        this.data = data;
    }

    public T getData() {
        return this.data;
    }

    public RegionComponentType<T> getType() {
        return this.type;
    }
}
