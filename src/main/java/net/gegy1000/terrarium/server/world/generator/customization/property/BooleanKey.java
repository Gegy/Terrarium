package net.gegy1000.terrarium.server.world.generator.customization.property;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import javax.annotation.Nullable;

public class BooleanKey extends PropertyKey<Boolean> {
    public BooleanKey(String identifier) {
        super(identifier, Boolean.class);
    }

    @Override
    public JsonElement serializeValue(PropertyValue<Boolean> value) {
        return new JsonPrimitive(value.get());
    }

    @Override
    public PropertyValue<Boolean> makeValue(Boolean value) {
        return new BooleanValue(value);
    }

    @Nullable
    @Override
    public PropertyValue<Boolean> parseValue(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
            return new BooleanValue(element.getAsBoolean());
        }
        return null;
    }
}
