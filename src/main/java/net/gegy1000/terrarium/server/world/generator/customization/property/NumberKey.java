package net.gegy1000.terrarium.server.world.generator.customization.property;

import com.google.gson.JsonElement;

import javax.annotation.Nullable;

public class NumberKey extends PropertyKey<Number> {
    public NumberKey(String identifier) {
        super(identifier, Number.class);
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
