package net.gegy1000.terrarium.server.world.generator.customization.widget;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.JsonUtils;
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
        List<CustomizationCategory> categories = new ArrayList<>();
        for (Map.Entry<String, JsonElement> child : root.entrySet()) {
            if (child.getValue().isJsonObject()) {
                String identifier = child.getKey();
                JsonObject categoryRoot = child.getValue().getAsJsonObject();
                categories.add(CustomizationCategory.parseCategory(widgetParseHandler, identifier, categoryRoot));
            } else {
                Terrarium.LOGGER.warn("Ignored invalid category element {}: {}, expected object", child.getKey(), child.getValue());
            }
        }
        return categories;
    }

    public static CustomizationCategory parseCategory(WidgetParseHandler widgetParseHandler, String identifier, JsonObject root) {
        List<CustomizationWidget> widgets = new ArrayList<>();
        JsonArray widgetsArray = JsonUtils.getJsonArray(root, "widgets");
        for (JsonElement widgetElement : widgetsArray) {
            if (widgetElement.isJsonObject()) {
                widgets.add(widgetParseHandler.parseWidget(widgetElement.getAsJsonObject()));
            } else {
                Terrarium.LOGGER.warn("Ignored invalid widget element {}, expected object", widgetElement);
            }
        }
        return new CustomizationCategory(identifier, widgets);
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
