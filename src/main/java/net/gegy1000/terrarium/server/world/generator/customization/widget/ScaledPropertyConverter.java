package net.gegy1000.terrarium.server.world.generator.customization.widget;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.world.json.InitObjectParser;
import net.gegy1000.terrarium.server.world.json.InvalidJsonException;
import net.gegy1000.terrarium.server.world.json.JsonValueParser;

public class ScaledPropertyConverter implements WidgetPropertyConverter {
    private final double scale;

    public ScaledPropertyConverter(double scale) {
        this.scale = scale;
    }

    @Override
    public double fromUser(double value) {
        return value / this.scale;
    }

    @Override
    public double toUser(double value) {
        return value * this.scale;
    }

    public static class Parser implements InitObjectParser<WidgetPropertyConverter> {
        @Override
        public WidgetPropertyConverter parse(JsonValueParser valueParser, JsonObject objectRoot) throws InvalidJsonException {
            return new ScaledPropertyConverter(valueParser.parseDouble(objectRoot, "scale"));
        }
    }
}
