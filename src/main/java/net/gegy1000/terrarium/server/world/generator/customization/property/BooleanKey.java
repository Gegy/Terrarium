package net.gegy1000.terrarium.server.world.generator.customization.property;

import com.google.gson.JsonElement;

import javax.annotation.Nullable;

public class BooleanKey extends PropertyKey<Boolean> {
    public BooleanKey(String identifier) {
        super(identifier, Boolean.class);
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
