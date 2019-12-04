package net.gegy1000.terrarium.server.world.generator.customization;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.gegy1000.gengen.api.GenericWorldType;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyValue;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GenerationSettings {
    private final ImmutableMap<String, PropertyKey<?>> keys;
    private final ImmutableMap<PropertyKey<?>, PropertyValue<?>> values;

    protected GenerationSettings(Map<String, PropertyKey<?>> keys, Map<PropertyKey<?>, PropertyValue<?>> values) {
        this.keys = ImmutableMap.copyOf(keys);
        this.values = ImmutableMap.copyOf(values);
    }

    public static GenerationSettings parse(World world) {
        String generatorOptions = world.getWorldInfo().getGeneratorOptions();

        TerrariumWorldType worldType = GenericWorldType.unwrapAs(world.getWorldType(), TerrariumWorldType.class);
        if (worldType != null) {
            PropertyPrototype prototype = worldType.buildPropertyPrototype();

            GenerationSettings parsedSettings = GenerationSettings.parse(prototype, generatorOptions);
            GenerationSettings defaultSettings = worldType.getPreset().createProperties(prototype);
            return defaultSettings.union(parsedSettings);
        }

        throw new IllegalArgumentException("Cannot parse settings for non-terrarium world type");
    }

    public static GenerationSettings parse(PropertyPrototype prototype, String json) {
        JsonElement element = new JsonParser().parse(json);
        if (element instanceof JsonObject) {
            return GenerationSettings.parse(prototype, element.getAsJsonObject());
        } else {
            return new GenerationSettings(new HashMap<>(), new HashMap<>());
        }
    }

    public static GenerationSettings parse(PropertyPrototype prototype, JsonObject root) {
        Map<String, PropertyKey<?>> keys = new HashMap<>();
        Map<PropertyKey<?>, PropertyValue<?>> values = new HashMap<>();

        for (Map.Entry<String, JsonElement> propertyEntry : root.entrySet()) {
            String identifier = propertyEntry.getKey();
            JsonElement propertyElement = propertyEntry.getValue();

            PropertyKey<?> key = prototype.getKey(identifier);
            if (key == null) {
                Terrarium.LOGGER.error("Ignored invalid property key '{}'", identifier);
                continue;
            }

            PropertyValue<?> value = key.parseValue(propertyElement);
            if (value == null) {
                Terrarium.LOGGER.error("Failed to parse invalid property with key '{}'", identifier);
                continue;
            }

            keys.put(key.getIdentifier(), key);
            values.put(key, value);
        }

        return new GenerationSettings(keys, values);
    }

    public JsonObject serialize() {
        JsonObject propertiesRoot = new JsonObject();

        for (PropertyKey<?> key : this.keys.values()) {
            JsonElement element = this.serializeProperty(key);
            propertiesRoot.add(key.getIdentifier(), element);
        }

        return propertiesRoot;
    }

    private <T> JsonElement serializeProperty(PropertyKey<T> key) {
        return key.serializeValue(this.getValue(key));
    }

    public String serializeString() {
        return this.serialize().toString();
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

    public <T> T get(PropertyKey<T> key) {
        PropertyValue<T> property = this.getValue(key);
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
