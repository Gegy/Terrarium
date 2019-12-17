package net.gegy1000.terrarium.server.world.generator.customization.property;

public final class PropertyPair<T> {
    public final PropertyKey<T> key;
    public final PropertyValue<T> value;

    private PropertyPair(PropertyKey<T> key, PropertyValue<T> value) {
        this.key = key;
        this.value = value;
    }

    public static <T> PropertyPair<T> of(PropertyKey<T> key, PropertyValue<T> value) {
        return new PropertyPair<>(key, value);
    }
}
