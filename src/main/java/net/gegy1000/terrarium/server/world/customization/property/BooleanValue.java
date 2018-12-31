package net.gegy1000.terrarium.server.world.customization.property;

import javax.annotation.Nonnull;

public class BooleanValue implements PropertyValue<Boolean> {
    private boolean value;

    public BooleanValue(boolean value) {
        this.value = value;
    }

    @Override
    public void set(@Nonnull Boolean value) {
        this.value = value;
    }

    @Override
    public Boolean get() {
        return this.value;
    }

    @Override
    public Class<Boolean> getType() {
        return Boolean.class;
    }
}
