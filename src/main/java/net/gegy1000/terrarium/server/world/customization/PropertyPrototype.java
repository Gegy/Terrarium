package net.gegy1000.terrarium.server.world.customization;

import com.google.common.collect.ImmutableMap;
import net.gegy1000.terrarium.server.world.customization.property.PropertyKey;

import javax.annotation.Nullable;
import java.util.Collection;

public class PropertyPrototype {
    public static final PropertyPrototype EMPTY = new PropertyPrototype(ImmutableMap.of());

    private final ImmutableMap<String, PropertyKey<?>> properties;

    private PropertyPrototype(ImmutableMap<String, PropertyKey<?>> properties) {
        this.properties = properties;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Nullable
    public PropertyKey<?> getKey(String identifier) {
        return this.properties.get(identifier);
    }

    public Collection<PropertyKey<?>> getProperties() {
        return this.properties.values();
    }

    public static class Builder {
        private final ImmutableMap.Builder<String, PropertyKey<?>> properties = new ImmutableMap.Builder<>();

        private Builder() {
        }

        public Builder withProperties(PropertyKey<?>... properties) {
            for (PropertyKey<?> key : properties) {
                this.properties.put(key.getIdentifier(), key);
            }
            return this;
        }

        public PropertyPrototype build() {
            return new PropertyPrototype(this.properties.build());
        }
    }
}
