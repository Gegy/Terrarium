package net.gegy1000.terrarium.server.world.customization.property;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

import javax.annotation.Nullable;

public class NumberKey extends PropertyKey<Number> {
    public NumberKey(String identifier) {
        super(identifier, Number.class);
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops, PropertyValue<Number> value) {
        return ops.createNumeric(value.get());
    }

    @Nullable
    @Override
    public <D> PropertyValue<Number> parseValue(Dynamic<D> element) {
        return element.getNumberValue()
                .map(n -> new NumberValue(n.doubleValue()))
                .orElse(null);
    }
}
