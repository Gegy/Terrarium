package net.gegy1000.terrarium.server.util;

import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.minecraft.world.World;

import java.util.function.Function;
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

    public static class WorldCap<T> extends Lazy<T> {
        public WorldCap(World world, Function<TerrariumWorldData, T> function) {
            super(() -> {
                TerrariumWorldData capability = world.getCapability(TerrariumCapabilities.worldDataCapability, null);
                if (capability != null) {
                    return function.apply(capability);
                }
                throw new IllegalStateException("Tried to get world capability before it was present");
            });
        }
    }
}
