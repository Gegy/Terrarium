package net.gegy1000.terrarium.server.world.pipeline.component;

public class RegionComponent<T> {
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
