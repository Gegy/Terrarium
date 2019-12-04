package net.gegy1000.terrarium.server.util;

import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.function.Function;
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

    public static <T> Lazy<T> worldCap(World world, Function<TerrariumWorld, T> function) {
        return new Lazy<>(() -> {
            TerrariumWorld capability = world.getCapability(TerrariumCapabilities.world(), null);
            if (capability != null) {
                return function.apply(capability);
            }
            throw new IllegalStateException("Tried to get world capability before it was present");
        });
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
