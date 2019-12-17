package net.gegy1000.earth.server.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.function.Function;
import java.util.function.IntFunction;

public final class Zoomable<T> {
    private final ZoomLevels levels;
    private final Int2ObjectMap<T> internal;
    private T orElse;

    private Zoomable(ZoomLevels levels, Int2ObjectMap<T> internal) {
        this.levels = levels;
        this.internal = internal;
    }

    public Zoomable<T> orElse(T value) {
        this.orElse = value;
        return this;
    }

    public static <T> Zoomable<T> create(ZoomLevels levels, IntFunction<T> function) {
        Int2ObjectMap<T> internal = new Int2ObjectOpenHashMap<>();
        levels.stream().forEach(zoom -> internal.put(zoom, function.apply(zoom)));

        return new Zoomable<>(levels, internal);
    }

    public T forZoom(int zoom) {
        return this.internal.getOrDefault(zoom, this.orElse);
    }

    public ZoomLevels getLevels() {
        return this.levels;
    }

    public boolean contains(int zoom) {
        return this.levels.contains(zoom);
    }

    public <R> Zoomable<R> map(Map<T, R> map) {
        Int2ObjectMap<R> result = new Int2ObjectOpenHashMap<>(this.internal.size());
        for (Int2ObjectMap.Entry<T> entry : this.internal.int2ObjectEntrySet()) {
            int zoom = entry.getIntKey();
            T value = entry.getValue();
            result.put(zoom, map.apply(zoom, value));
        }
        return new Zoomable<>(this.levels, result);
    }

    public <R> Zoomable<R> mapValue(Function<T, R> map) {
        return this.map((zoom, value) -> map.apply(value));
    }

    public interface Map<T, R> {
        R apply(int zoom, T value);
    }
}
