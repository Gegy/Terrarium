package net.gegy1000.terrarium.server.world.generator.customization.widget;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.event.TerrariumRegistryEvent;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.json.InitObjectParser;
import net.gegy1000.terrarium.server.world.json.InvalidJsonException;
import net.gegy1000.terrarium.server.world.json.JsonValueParser;
import net.gegy1000.terrarium.server.world.json.ParseStateHandler;
import net.gegy1000.terrarium.server.world.json.ParseUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Terrarium.MODID)
public class WidgetParseHandler {
    private static final Map<ResourceLocation, WidgetParser<?>> WIDGET_PARSERS = new HashMap<>();

    private final Map<String, PropertyKey<?>> properties;
    private final JsonValueParser constantParser;

    public WidgetParseHandler(Map<String, PropertyKey<?>> properties, JsonValueParser constantParser) {
        this.properties = ImmutableMap.copyOf(properties);
        this.constantParser = constantParser;
    }

    public static void onInit() {
        MinecraftForge.EVENT_BUS.post(new Event(WIDGET_PARSERS));
    }

    @SubscribeEvent
    public static void onRegisterParsers(Event event) {
        event.register(new ResourceLocation(Terrarium.MODID, "toggle"), (WidgetParser<Boolean>) (widgetRoot, propertyKey, valueParser) -> new ToggleWidget(propertyKey));

        event.register(new ResourceLocation(Terrarium.MODID, "slider"), (WidgetParser<Number>) (widgetRoot, propertyKey, valueParser) -> {
            JsonArray rangeArray = ParseUtils.getJsonArray(widgetRoot, "range");
            JsonArray stepArray = ParseUtils.getJsonArray(widgetRoot, "step");

            double minimum = rangeArray.get(0).getAsDouble();
            double maximum = rangeArray.get(1).getAsDouble();
            double step = stepArray.get(0).getAsDouble();
            double fineStep = stepArray.get(1).getAsDouble();

            WidgetPropertyConverter converter = null;
            if (widgetRoot.has("value_converter")) {
                converter = ParseUtils.parseObject(widgetRoot, "value_converter", converterRoot -> {
                    ResourceLocation converterType = new ResourceLocation(ParseUtils.getString(converterRoot, "type"));
                    InitObjectParser<WidgetPropertyConverter> parser = WidgetConverterRegistry.get(converterType);
                    return parser.parse(valueParser, converterRoot);
                });
            }

            return new SliderWidget(propertyKey, minimum, maximum, step, fineStep, converter);
        });
    }

    @SuppressWarnings("unchecked")
    public CustomizationWidget parseWidget(JsonObject root) throws InvalidJsonException {
        String propertyKey = ParseUtils.getString(root, "property").toLowerCase(Locale.ROOT);

        try {
            ParseStateHandler.pushContext("parsing widget with property \"" + propertyKey + "\"");

            ResourceLocation type = new ResourceLocation(ParseUtils.getString(root, "type"));

            PropertyKey property = this.properties.get(propertyKey);
            if (property == null) {
                throw new InvalidJsonException("Could not find property " + propertyKey);
            }

            try {
                WidgetParser<?> parser = WIDGET_PARSERS.get(type);
                if (parser == null) {
                    throw new InvalidJsonException("Widget parser with key \"" + type + "\" did not exist");
                }

                return parser.parse(root, property, this.constantParser);
            } catch (ClassCastException e) {
                throw new InvalidJsonException("Property " + propertyKey + " of wrong type " + property.getType());
            }
        } finally {
            ParseStateHandler.popContext();
        }
    }

    public static Map<ResourceLocation, WidgetParser<?>> getRegistry() {
        return Collections.unmodifiableMap(WIDGET_PARSERS);
    }

    public static final class Event extends TerrariumRegistryEvent<WidgetParser<?>> {
        private Event(Map<ResourceLocation, WidgetParser<?>> registry) {
            super(registry);
        }
    }
}
