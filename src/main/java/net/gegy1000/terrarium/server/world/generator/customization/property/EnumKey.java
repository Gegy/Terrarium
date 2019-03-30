package net.gegy1000.terrarium.server.world.generator.customization.property;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class EnumKey<T extends Enum & CycleEnumProperty> extends PropertyKey<T> {
    private final Map<String, T> lookup = new HashMap<>();

    public EnumKey(String identifier, Class<T> type) {
        super(identifier, type);
        for (T constant : type.getEnumConstants()) {
            this.lookup.put(constant.getKey(), constant);
        }
    }

    @Override
    public JsonElement serializeValue(PropertyValue<T> value) {
        return new JsonPrimitive(value.get().getKey());
    }

    @Nullable
    @Override
    public PropertyValue<T> parseValue(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String key = element.getAsString();
            T value = this.lookup.get(key);
            if (value != null) {
                return new EnumValue<>(this.getType(), value);
            }
        }
        return null;
    }
}
