package net.gegy1000.terrarium.server.world.pipeline.data;

import net.minecraft.util.ResourceLocation;

public final class DataKey<T extends Data> {
    private final ResourceLocation identifier;

    public DataKey(ResourceLocation identifier) {
        this.identifier = identifier;
    }

    public final ResourceLocation getIdentifier() {
        return this.identifier;
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
}
