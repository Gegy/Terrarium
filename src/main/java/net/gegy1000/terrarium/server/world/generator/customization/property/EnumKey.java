package net.gegy1000.terrarium.server.world.generator.customization.property;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.annotation.Nullable;
import java.util.Map;

public class EnumKey<T extends Enum<T> & CycleEnumProperty> extends PropertyKey<T> {
    private final Map<String, T> lookup = new Object2ObjectOpenHashMap<>();

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

    @Override
    public PropertyValue<T> makeValue(T value) {
        return new EnumValue<T>(this.getType(), value);
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
