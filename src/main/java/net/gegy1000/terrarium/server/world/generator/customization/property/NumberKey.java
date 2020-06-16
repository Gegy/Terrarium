package net.gegy1000.terrarium.server.world.generator.customization.property;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import javax.annotation.Nullable;

public class NumberKey extends PropertyKey<Number> {
    public NumberKey(String identifier) {
        super(identifier, Number.class);
    }

    @Override
    public JsonElement serializeValue(PropertyValue<Number> value) {
        return new JsonPrimitive(value.get());
    }

    @Override
    public PropertyValue<Number> makeValue(Number value) {
        return new NumberValue(value.doubleValue());
    }

    @Nullable
    @Override
    public PropertyValue<Number> parseValue(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return new NumberValue(element.getAsDouble());
        }
        return null;
    }
}
