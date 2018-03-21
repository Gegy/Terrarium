package net.gegy1000.terrarium.server.world.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import net.gegy1000.terrarium.server.world.generator.customization.PropertyContainer;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyValue;
import net.minecraft.block.state.IBlockState;

public abstract class PropertyJsonValueParser implements JsonValueParser {
    @Override
    public boolean parseBoolean(JsonObject objectRoot, String key) {
        JsonElement element = objectRoot.get(key);
        if (element == null) {
            throw new JsonSyntaxException("Boolean element with key " + key + " did not exist");
        }

        Boolean providedValue = this.parseFromProvider(element);
        if (providedValue != null) {
            return providedValue;
        }

        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            PropertyValue<Boolean> value = this.parseProperty(primitive, Boolean.class);
            if (value != null) {
                return value.get();
            }
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            }
        }

        throw new JsonSyntaxException("Could not parse boolean from non-boolean element: " + element);
    }

    @Override
    public double parseDouble(JsonObject objectRoot, String key) {
        JsonElement element = objectRoot.get(key);
        if (element == null) {
            throw new JsonSyntaxException("Double element with key " + key + " did not exist");
        }

        Double providedValue = this.parseFromProvider(element);
        if (providedValue != null) {
            return providedValue;
        }

        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            PropertyValue<Number> value = this.parseProperty(primitive, Number.class);
            if (value != null) {
                return value.get().doubleValue();
            }
            if (primitive.isNumber()) {
                return primitive.getAsDouble();
            }
        }

        throw new JsonSyntaxException("Could not parse double from non-double element: " + element);
    }

    @Override
    public int parseInteger(JsonObject objectRoot, String key) {
        JsonElement element = objectRoot.get(key);
        if (element == null) {
            throw new JsonSyntaxException("Integer element with key " + key + " did not exist");
        }

        Integer providedValue = this.parseFromProvider(element);
        if (providedValue != null) {
            return providedValue;
        }

        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            PropertyValue<Number> value = this.parseProperty(primitive, Number.class);
            if (value != null) {
                return value.get().intValue();
            }
            if (primitive.isNumber()) {
                return primitive.getAsInt();
            }
        }

        throw new JsonSyntaxException("Could not parse integer from non-integer element: " + element);
    }

    @Override
    public String parseString(JsonObject objectRoot, String key) {
        JsonElement element = objectRoot.get(key);
        if (element == null) {
            throw new JsonSyntaxException("String element with key " + key + " did not exist");
        }

        String providedValue = this.parseFromProvider(element);
        if (providedValue != null) {
            return providedValue;
        }

        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            PropertyValue<String> value = this.parseProperty(primitive, String.class);
            if (value != null) {
                return value.get();
            }
            if (primitive.isString()) {
                return primitive.getAsString();
            }
        }

        throw new JsonSyntaxException("Could not parse string from non-string: " + element);
    }

    @Override
    public IBlockState parseBlockState(JsonObject objectRoot, String key) {
        String blockstateKey = this.parseString(objectRoot, key);
        return TerrariumJsonUtils.parseBlockState(blockstateKey);
    }

    protected <T> PropertyValue<T> parseProperty(JsonPrimitive primitive, Class<T> type) {
        if (primitive.isString()) {
            String identifier = primitive.getAsString();
            if (identifier.startsWith("@")) {
                String keyIdentifier = identifier.substring(1);
                return this.getProperty(keyIdentifier, type);
            }
        }
        return null;
    }

    protected abstract <T> PropertyValue<T> getProperty(String keyIdentifier, Class<T> type);

    public static class Container extends PropertyJsonValueParser {
        private final PropertyContainer container;

        public Container(PropertyContainer container) {
            this.container = container;
        }

        @Override
        protected <T> PropertyValue<T> getProperty(String keyIdentifier, Class<T> type) {
            if (this.container.hasKey(keyIdentifier)) {
                PropertyKey<T> key = this.container.getKey(keyIdentifier, type);
                return this.container.getValue(key);
            }
            return null;
        }
    }
}
