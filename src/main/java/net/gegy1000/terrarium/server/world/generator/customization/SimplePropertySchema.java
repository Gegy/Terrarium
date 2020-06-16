package net.gegy1000.terrarium.server.world.generator.customization;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyPair;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyValue;

import java.util.Map;
import java.util.function.Consumer;

public final class SimplePropertySchema implements PropertySchema {
    private final ImmutableMap<String, PropertyKey<?>> properties;

    private SimplePropertySchema(ImmutableMap<String, PropertyKey<?>> properties) {
        this.properties = properties;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean parse(Map<String, JsonElement> entries, Consumer<PropertyPair<?>> consumer) {
        boolean success = true;

        for (Map.Entry<String, JsonElement> entry : entries.entrySet()) {
            String keyId = entry.getKey();
            JsonElement jsonValue = entry.getValue();

            PropertyKey<?> key = this.properties.get(keyId);
            if (key == null) {
                Terrarium.LOGGER.warn("Ignored unknown property key '{}'", keyId);
                success = false;
                continue;
            }

            PropertyValue<?> value = key.parseValue(jsonValue);
            if (value == null) {
                Terrarium.LOGGER.warn("Failed to parse property with key '{}' (={})", keyId, jsonValue);
                success = false;
                continue;
            }

            consumer.accept(PropertyPair.ofUnchecked(key, value));
        }

        return success;
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

        public SimplePropertySchema build() {
            return new SimplePropertySchema(this.properties.build());
        }
    }
}
