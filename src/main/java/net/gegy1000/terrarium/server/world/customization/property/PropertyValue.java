package net.gegy1000.terrarium.server.world.customization.property;

import javax.annotation.Nonnull;

public interface PropertyValue<T> {
    void set(@Nonnull T value);

    T get();

    Class<T> getType();
}
