package net.gegy1000.terrarium.server.world.data;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Function;

public final class DataKey<T> {
    private static int keyId;

    public final int id;
    private final ResourceLocation identifier;
    private final Function<DataView, T> createDefault;

    private DataKey(int id, ResourceLocation identifier, Function<DataView, T> createDefault) {
        this.id = id;
        this.identifier = identifier;
        this.createDefault = createDefault;
    }

    public static <T> DataKey<T> create(ResourceLocation identifier, Function<DataView, T> createDefault) {
        int id = keyId++;
        return new DataKey<>(id, identifier, createDefault);
    }

    public ResourceLocation getIdentifier() {
        return this.identifier;
    }

    @Nonnull
    public T createDefault(DataView view) {
        return this.createDefault.apply(view);
    }

    @Override
    public int hashCode() {
        return this.identifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof DataKey && ((DataKey) obj).id == this.id;
    }

    @Override
    public String toString() {
        return this.identifier.toString();
    }

    public static int keyCount() {
        return keyId;
    }
}
