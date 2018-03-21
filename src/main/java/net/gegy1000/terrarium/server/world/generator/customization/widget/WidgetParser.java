package net.gegy1000.terrarium.server.world.generator.customization.widget;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.json.JsonValueParser;

public interface WidgetParser<T> {
    CustomizationWidget parse(JsonObject widgetRoot, PropertyKey<T> propertyKey, JsonValueParser valueParser);
}
