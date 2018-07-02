package net.gegy1000.terrarium.server.world.generator.customization;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.generator.customization.property.BooleanValue;
import net.gegy1000.terrarium.server.world.generator.customization.property.NumberValue;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyValue;
import net.minecraft.util.Tuple;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PropertyContainer {
    private final ImmutableMap<String, PropertyKey<?>> keys;
    private final ImmutableMap<PropertyKey<?>, PropertyValue<?>> values;

    protected PropertyContainer(Map<String, PropertyKey<?>> keys, Map<PropertyKey<?>, PropertyValue<?>> values) {
        this.keys = ImmutableMap.copyOf(keys);
        this.values = ImmutableMap.copyOf(values);
    }

    public static PropertyContainer deserialize(JsonObject root) {
        Map<String, PropertyKey<?>> keys = new HashMap<>();
        Map<PropertyKey<?>, PropertyValue<?>> values = new HashMap<>();

        for (Map.Entry<String, JsonElement> propertyEntry : root.entrySet()) {
            String identifier = propertyEntry.getKey();
            JsonElement propertyElement = propertyEntry.getValue();

            Tuple<PropertyKey<?>, PropertyValue<?>> pair = PropertyContainer.parseKeyValuePair(identifier, propertyElement);
            if (pair != null) {
                PropertyKey<?> key = pair.getFirst();
                PropertyValue<?> value = pair.getSecond();
                keys.put(key.getIdentifier(), key);
                values.put(key, value);
            } else {
                Terrarium.LOGGER.warn("Ignored invalid property {}: {}", identifier, propertyElement);
            }
        }

        return new PropertyContainer(keys, values);
    }

    private static Tuple<PropertyKey<?>, PropertyValue<?>> parseKeyValuePair(String identifier, JsonElement element) {
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isNumber()) {
                return new Tuple<>(PropertyKey.createNumber(identifier), new NumberValue(primitive.getAsDouble()));
            } else if (primitive.isBoolean()) {
                return new Tuple<>(PropertyKey.createBoolean(identifier), new BooleanValue(primitive.getAsBoolean()));
            }
        }
        return null;
    }

    public JsonObject serialize() {
        JsonObject propertiesRoot = new JsonObject();

        for (Map.Entry<PropertyKey<?>, PropertyValue<?>> entry : this.values.entrySet()) {
            PropertyKey<?> key = entry.getKey();
            PropertyValue<?> value = entry.getValue();
            if (Number.class.isAssignableFrom(key.getType())) {
                propertiesRoot.addProperty(key.getIdentifier(), (Number) value.get());
            } else if (Boolean.class.isAssignableFrom(key.getType())) {
                propertiesRoot.addProperty(key.getIdentifier(), (Boolean) value.get());
            }
        }

        return propertiesRoot;
    }

    public boolean hasKey(String identifier) {
        return this.keys.containsKey(identifier);
    }

    @SuppressWarnings("unchecked")
    public <T> PropertyKey<T> getKey(String identifier, Class<T> type) {
        PropertyKey<?> key = this.keys.get(identifier);
        if (key != null && key.getType().isAssignableFrom(type)) {
            return (PropertyKey<T>) key;
        }
        throw new IllegalArgumentException("Given property identifier does not exist: " + identifier);
    }

    @SuppressWarnings("unchecked")
    public <T> PropertyValue<T> getValue(PropertyKey<T> key) {
        PropertyValue<?> value = this.values.get(key);
        if (value != null && value.getType().equals(key.getType())) {
            return (PropertyValue<T>) value;
        }
        throw new IllegalArgumentException("Given property key does not exist: " + key.getIdentifier());
    }

    public void setDouble(PropertyKey<Number> key, double value) {
        PropertyValue<Number> property = this.getValue(key);
        property.set(value);
    }

    public double getDouble(PropertyKey<Number> key) {
        PropertyValue<Number> property = this.getValue(key);
        return property.get().doubleValue();
    }

    public void setInteger(PropertyKey<Number> key, int value) {
        PropertyValue<Number> property = this.getValue(key);
        property.set(value);
    }

    public int getInteger(PropertyKey<Number> key) {
        PropertyValue<Number> property = this.getValue(key);
        return property.get().intValue();
    }

    public void setBoolean(PropertyKey<Boolean> key, boolean value) {
        PropertyValue<Boolean> property = this.getValue(key);
        property.set(value);
    }

    public boolean getBoolean(PropertyKey<Boolean> key) {
        PropertyValue<Boolean> property = this.getValue(key);
        return property.get();
    }

    public boolean hasKey(PropertyKey<?> key) {
        return this.values.containsKey(key);
    }

    public Collection<PropertyKey<?>> getKeys() {
        return this.keys.values();
    }
}
