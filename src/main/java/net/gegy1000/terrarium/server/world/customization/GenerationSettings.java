package net.gegy1000.terrarium.server.world.customization;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.customization.property.PropertyValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GenerationSettings {
    private final ImmutableMap<String, PropertyKey<?>> keys;
    private final ImmutableMap<PropertyKey<?>, PropertyValue<?>> values;

    protected GenerationSettings(Map<String, PropertyKey<?>> keys, Map<PropertyKey<?>, PropertyValue<?>> values) {
        this.keys = ImmutableMap.copyOf(keys);
        this.values = ImmutableMap.copyOf(values);
    }

    public static <T> GenerationSettings deserialize(PropertyPrototype prototype, Dynamic<T> root) {
        Map<String, PropertyKey<?>> keys = new HashMap<>();
        Map<PropertyKey<?>, PropertyValue<?>> values = new HashMap<>();

        Optional<Map<Dynamic<T>, Dynamic<T>>> mapValues = root.getMapValues();
        if (mapValues.isPresent()) {
            for (Map.Entry<Dynamic<T>, Dynamic<T>> entry : mapValues.get().entrySet()) {
                String identifier = entry.getKey().getStringValue().orElse("");

                PropertyKey<?> key = prototype.getKey(identifier);
                if (key == null) {
                    Terrarium.LOGGER.error("Ignored invalid property key '{}'", identifier);
                    continue;
                }

                PropertyValue<?> value = key.parseValue(entry.getValue());
                if (value == null) {
                    Terrarium.LOGGER.error("Failed to parse invalid property with key '{}'", identifier);
                    continue;
                }

                keys.put(key.getIdentifier(), key);
                values.put(key, value);
            }
        }

        return new GenerationSettings(keys, values);
    }

    public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
        Map<T, T> properties = new HashMap<>();

        for (Map.Entry<PropertyKey<?>, PropertyValue<?>> entry : this.values.entrySet()) {
            PropertyKey<?> key = entry.getKey();
            PropertyValue<?> value = entry.getValue();
            T identifier = ops.createString(key.getIdentifier());
            properties.put(identifier, this.serialize(ops, key, value));
        }

        return new Dynamic<>(ops, ops.createMap(properties));
    }

    @SuppressWarnings("unchecked")
    private <T, D> D serialize(DynamicOps<D> ops, PropertyKey<T> key, PropertyValue<?> value) {
        return key.serialize(ops, (PropertyValue<T>) value);
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

    public GenerationSettings union(GenerationSettings other) {
        Map<String, PropertyKey<?>> keys = new HashMap<>(this.keys);
        Map<PropertyKey<?>, PropertyValue<?>> values = new HashMap<>(this.values);
        keys.putAll(other.keys);
        values.putAll(other.values);

        return new GenerationSettings(keys, values);
    }
}
