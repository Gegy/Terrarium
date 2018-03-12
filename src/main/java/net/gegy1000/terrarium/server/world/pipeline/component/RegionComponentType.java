package net.gegy1000.terrarium.server.world.pipeline.component;

public abstract class RegionComponentType<T> {
    private final Class<T> type;

    public RegionComponentType(Class<T> type) {
        this.type = type;
    }

    public abstract T createDefaultData(int width, int height);

    public final Class<T> getType() {
        return this.type;
    }

    @Override
    public final int hashCode() {
        return this.type.getName().hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        return obj instanceof RegionComponentType && ((RegionComponentType) obj).getType().equals(this.type);
    }
}
