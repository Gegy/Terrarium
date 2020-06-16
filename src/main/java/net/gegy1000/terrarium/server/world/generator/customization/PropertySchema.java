package net.gegy1000.terrarium.server.world.generator.customization;

import com.google.gson.JsonElement;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyPair;

import java.util.Map;
import java.util.function.Consumer;

public interface PropertySchema {
    boolean parse(Map<String, JsonElement> entries, Consumer<PropertyPair<?>> consumer);
}
