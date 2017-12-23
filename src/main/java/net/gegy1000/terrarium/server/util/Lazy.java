package net.gegy1000.terrarium.server.util;

import java.util.function.Supplier;

public class Lazy<T> {
    private final Supplier<T> supplier;

    private T value;
    private boolean present;

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (!this.present) {
            this.value = this.supplier.get();
            this.present = true;
        }
        return this.value;
    }
}
