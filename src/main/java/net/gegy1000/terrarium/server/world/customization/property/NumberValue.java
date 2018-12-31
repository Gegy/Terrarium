package net.gegy1000.terrarium.server.world.customization.property;

import javax.annotation.Nonnull;

public class NumberValue implements PropertyValue<Number> {
    private double value;

    public NumberValue(double value) {
        this.value = value;
    }

    @Override
    public void set(@Nonnull Number value) {
        this.value = value.doubleValue();
    }

    @Override
    public Number get() {
        return this.value;
    }

    @Override
    public Class<Number> getType() {
        return Number.class;
    }
}
