package net.gegy1000.terrarium.server.world.generator.customization;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;

public interface WidgetParser<T> {
    CustomizationWidget parse(JsonObject widgetRoot, PropertyKey<T> propertyKey);
}
