package net.gegy1000.terrarium.server.world.customization;

import net.gegy1000.terrarium.server.world.customization.property.PropertyValue;
import net.gegy1000.terrarium.server.world.customization.property.PropertyKey;

public class PropertyPair<T> {
    private final PropertyKey<T> key;
    private final PropertyValue<T> value;

    public PropertyPair(PropertyKey<T> key, PropertyValue<T> value) {
        this.key = key;
        this.value = value;
    }

    public PropertyKey<T> getKey() {
        return this.key;
    }

    public PropertyValue<T> getValue() {
        return this.value;
    }
}
