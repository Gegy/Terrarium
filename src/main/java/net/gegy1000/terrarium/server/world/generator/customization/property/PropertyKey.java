package net.gegy1000.terrarium.server.world.generator.customization.property;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class PropertyKey<T> {
    private final String identifier;
    private final Class<T> type;

    public PropertyKey(String identifier, Class<T> type) {
        this.identifier = identifier;
        this.type = type;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public Class<T> getType() {
        return this.type;
    }

    @Override
    public int hashCode() {
        return this.identifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PropertyKey && obj.getClass() == this.getClass() && ((PropertyKey) obj).getIdentifier().equals(this.identifier);
    }

    public static Map<String, PropertyKey<?>> parseProperties(JsonObject root) {
        Map<String, PropertyKey<?>> properties = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> child : root.entrySet()) {
            JsonElement element = child.getValue();
            if (element.isJsonPrimitive()) {
                String identifier = child.getKey();
                PropertyKey<?> key = PropertyKey.createKey(identifier, element.getAsString());
                if (key != null) {
                    properties.put(identifier, key);
                } else {
                    Terrarium.LOGGER.warn("Could not parse property {} of data type {}", identifier, element.getAsString());
                }
            } else {
                Terrarium.LOGGER.warn("Ignored invalid property element {}: {}, expected string", child.getKey(), child.getValue());
            }
        }
        return properties;
    }

    public static PropertyKey<?> createKey(String identifier, String dataType) {
        dataType = dataType.toLowerCase(Locale.ROOT);
        switch (dataType) {
            case "number":
            case "double":
            case "float":
            case "int":
            case "integer":
                return new PropertyKey<>(identifier, Number.class);
            case "boolean":
            case "bool":
            case "flag":
                return new PropertyKey<>(identifier, Boolean.class);
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    public String getLocalizedName() {
        return I18n.format("property.terrarium." + this.identifier + ".name");
    }

    @SideOnly(Side.CLIENT)
    public String getLocalizedTooltip() {
        return I18n.format("property.terrarium." + this.identifier + ".tooltip");
    }
}
