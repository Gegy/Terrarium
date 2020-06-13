package net.gegy1000.terrarium.server.world.generator.customization.property;

import javax.annotation.Nonnull;

public class EnumValue<T extends Enum<T> & CycleEnumProperty> implements PropertyValue<T> {
    private final Class<T> type;
    private T value;

    public EnumValue(Class<T> type, T value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public void set(@Nonnull T value) {
        this.value = value;
    }

    @Override
    public T get() {
        return this.value;
    }

    @Override
    public Class<T> getType() {
        return this.type;
    }
}
