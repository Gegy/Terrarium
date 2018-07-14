package net.gegy1000.terrarium.server.world.generator.customization;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

public class GenerationSettings {
    private final ResourceLocation worldType;
    private final PropertyContainer propertyContainer;

    private GenerationSettings(ResourceLocation worldType, PropertyContainer propertyContainer) {
        this.worldType = worldType;
        this.propertyContainer = propertyContainer;
    }

    public static GenerationSettings deserialize(String json) {
        return GenerationSettings.deserialize(new JsonParser().parse(json).getAsJsonObject());
    }

    public static GenerationSettings deserialize(JsonObject root) {
        ResourceLocation worldType = new ResourceLocation(JsonUtils.getString(root, "world_type"));
        PropertyContainer properties = PropertyContainer.deserialize(root.getAsJsonObject("properties"));

        return new GenerationSettings(worldType, properties);
    }

    public JsonObject serialize() {
        JsonObject root = new JsonObject();

        root.addProperty("world_type", this.worldType.toString());
        root.add("properties", this.propertyContainer.serialize());

        return root;
    }

    public String serializeString() {
        return new Gson().toJson(this.serialize());
    }

    public ResourceLocation getWorldType() {
        return this.worldType;
    }

    public PropertyContainer getProperties() {
        return this.propertyContainer;
    }

    public GenerationSettings copy() {
        try {
            return GenerationSettings.deserialize(this.serialize());
        } catch (JsonSyntaxException e) {
            throw new IllegalStateException("Failed to parsed copied settings", e);
        }
    }

    public GenerationSettings union(GenerationSettings other) {
        PropertyContainer properties = this.propertyContainer.union(other.getProperties());
        return new GenerationSettings(other.getWorldType(), properties);
    }
}
