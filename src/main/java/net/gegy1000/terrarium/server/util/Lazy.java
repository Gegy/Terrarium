package net.gegy1000.terrarium.server.util;

import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Lazy<T> {
    private Supplier<T> supplier;
    private T value;

    private Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    public static <O extends ICapabilityProvider, T> Lazy<Optional<T>> ofCapability(O object, Capability<T> capability) {
        return Lazy.of(() -> Optional.ofNullable(object.getCapability(capability, null)));
    }

    public static <T extends IForgeRegistryEntry<T>> Lazy<Optional<T>> ofRegistry(IForgeRegistry<T> registry, ResourceLocation key) {
        return Lazy.of(() -> Optional.ofNullable(registry.getValue(key)));
    }

    public <U> Lazy<U> map(Function<T, U> op) {
        return Lazy.of(() -> op.apply(this.get()));
    }

    @Nonnull
    public T get() {
        if (this.value == null) {
            this.value = Preconditions.checkNotNull(this.supplier.get(), "lazy result was null");
            this.supplier = null;
        }
        return this.value;
    }
}
