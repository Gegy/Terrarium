package net.gegy1000.terrarium.server.util;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Supplier;

public final class Lazy<T> {
    private final Supplier<T> supplier;

    private T value;
    private boolean present;

    private Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    public static <O extends ICapabilityProvider, T> Lazy<Optional<T>> ofCapability(O object, Capability<T> capability) {
        return Lazy.of(() -> Optional.ofNullable(object.getCapability(capability, null)));
    }

    @Nonnull
    public T get() {
        if (!this.present) {
            this.value = this.supplier.get();
            this.present = true;
        }
        return this.value;
    }
}
