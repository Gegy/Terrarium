package net.gegy1000.terrarium.server.world.generator.customization.widget;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.world.json.InvalidJsonException;
import net.gegy1000.terrarium.server.world.json.ParseStateHandler;
import net.gegy1000.terrarium.server.world.json.ParseUtils;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomizationCategory {
    private final String identifier;
    private final ImmutableList<CustomizationWidget> widgets;

    public CustomizationCategory(String identifier, List<CustomizationWidget> widgets) {
        this.identifier = identifier;
        this.widgets = ImmutableList.copyOf(widgets);
    }

    public String getIdentifier() {
        return this.identifier;
    }

    @SideOnly(Side.CLIENT)
    public String getLocalizedName() {
        return I18n.format("category.terrarium." + this.identifier + ".name");
    }

    public ImmutableList<CustomizationWidget> getWidgets() {
        return this.widgets;
    }

    public static List<CustomizationCategory> parseCategories(WidgetParseHandler widgetParseHandler, JsonObject root) {
        ParseStateHandler.pushContext("Parsing categories");

        List<CustomizationCategory> categories = new ArrayList<>();
        for (Map.Entry<String, JsonElement> child : root.entrySet()) {
            if (child.getValue().isJsonObject()) {
                try {
                    String identifier = child.getKey();
                    JsonObject categoryRoot = child.getValue().getAsJsonObject();
                    categories.add(CustomizationCategory.parseCategory(widgetParseHandler, identifier, categoryRoot));
                } catch (InvalidJsonException e) {
                    ParseStateHandler.error(e);
                }
            } else {
                ParseStateHandler.error("Ignored invalid category element: \"" + child.getKey() + "\"");
            }
        }

        ParseStateHandler.popContext();

        return categories;
    }

    public static CustomizationCategory parseCategory(WidgetParseHandler widgetParseHandler, String identifier, JsonObject root) throws InvalidJsonException {
        return new CustomizationCategory(identifier, ParseUtils.parseArray(root, "widgets", widgetsArray -> {
            List<CustomizationWidget> widgets = new ArrayList<>();
            for (JsonElement widgetElement : widgetsArray) {
                try {
                    if (widgetElement.isJsonObject()) {
                        widgets.add(widgetParseHandler.parseWidget(widgetElement.getAsJsonObject()));
                    } else {
                        ParseStateHandler.error("Ignored invalid widget element");
                    }
                } catch (InvalidJsonException e) {
                    ParseStateHandler.error(e);
                }
            }
            return widgets;
        }));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CustomizationCategory && ((CustomizationCategory) obj).identifier.equals(this.identifier);
    }

    @Override
    public int hashCode() {
        return this.identifier.hashCode();
    }
}
