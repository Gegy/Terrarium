package net.gegy1000.terrarium.server.world.pipeline.data;

public interface MergableData<T extends MergableData<T>> extends Data {
    T merge(T other);
}
