package net.gegy1000.earth.server.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.function.IntFunction;
import java.util.stream.IntStream;

public final class Zoomed<T> {
    private final Int2ObjectMap<T> internal;
    private T orElse;

    private Zoomed(Int2ObjectMap<T> internal) {
        this.internal = internal;
    }

    public Zoomed<T> orElse(T value) {
        this.orElse = value;
        return this;
    }

    public static <T> Zoomed<T> create(IntStream zooms, IntFunction<T> function) {
        Int2ObjectMap<T> internal = new Int2ObjectOpenHashMap<>();
        zooms.forEach(zoom -> internal.put(zoom, function.apply(zoom)));

        return new Zoomed<>(internal);
    }

    public T forZoom(int zoom) {
        return this.internal.getOrDefault(zoom, this.orElse);
    }
}
