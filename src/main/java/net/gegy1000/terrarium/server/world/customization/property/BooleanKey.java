package net.gegy1000.terrarium.server.world.customization.property;

import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

import javax.annotation.Nullable;

public class BooleanKey extends PropertyKey<Boolean> {
    public BooleanKey(String identifier) {
        super(identifier, Boolean.class);
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops, PropertyValue<Boolean> value) {
        return ops.createBoolean(value.get());
    }

    @Nullable
    @Override
    public <D> PropertyValue<Boolean> parseValue(Dynamic<D> element) {
        // Booleans are not supported by JsonOps
        if (element.getValue() instanceof JsonPrimitive) {
            JsonPrimitive primitive = (JsonPrimitive) element.getValue();
            if (primitive.isBoolean()) {
                return new BooleanValue(primitive.getAsBoolean());
            }
        }

        return element.getNumberValue()
                .map(n -> new BooleanValue(n.intValue() != 0))
                .orElse(null);
    }
}
