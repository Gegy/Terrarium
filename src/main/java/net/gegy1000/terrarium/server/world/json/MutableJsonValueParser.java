package net.gegy1000.terrarium.server.world.json;

import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyValue;

import java.util.Map;

public class MutableJsonValueParser extends PropertyJsonValueParser {
    private final Map<String, PropertyKey<?>> keys;
    private final Map<PropertyKey<?>, PropertyValue<?>> values;

    public MutableJsonValueParser(Map<String, PropertyKey<?>> keys, Map<PropertyKey<?>, PropertyValue<?>> values) {
        this.keys = keys;
        this.values = values;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> PropertyValue<T> getProperty(String keyIdentifier, Class<T> type) throws InvalidJsonException {
        if (this.keys.containsKey(keyIdentifier)) {
            PropertyKey<?> key = this.keys.get(keyIdentifier);
            if (!key.getType().isAssignableFrom(type)) {
                throw new InvalidJsonException("Tried to get property " + keyIdentifier + " of wrong type " + type);
            }
            return (PropertyValue<T>) this.values.get(key);
        }
        return null;
    }
}
