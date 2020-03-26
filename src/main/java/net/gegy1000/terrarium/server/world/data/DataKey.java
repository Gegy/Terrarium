package net.gegy1000.terrarium.server.world.data;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Function;

public final class DataKey<T> {
    private final ResourceLocation identifier;
    private final Function<DataView, T> createDefault;

    public DataKey(ResourceLocation identifier, Function<DataView, T> createDefault) {
        this.identifier = identifier;
        this.createDefault = createDefault;
    }

    public final ResourceLocation getIdentifier() {
        return this.identifier;
    }

    @Nonnull
    public T createDefault(DataView view) {
        return this.createDefault.apply(view);
    }

    @Override
    public final int hashCode() {
        return this.identifier.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof DataKey && ((DataKey) obj).getIdentifier().equals(this.identifier);
    }

    @Override
    public String toString() {
        return this.identifier.toString();
    }
}
