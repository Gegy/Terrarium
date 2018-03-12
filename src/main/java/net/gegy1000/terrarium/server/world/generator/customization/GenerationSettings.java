package net.gegy1000.terrarium.server.world.generator.customization;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.gegy1000.terrarium.server.world.generator.TerrariumGenerator;
import net.gegy1000.terrarium.server.world.generator.TerrariumGeneratorRegistry;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

public class GenerationSettings {
    private final TerrariumGenerator generator;
    private final PropertyContainer propertyContainer;

    private GenerationSettings(TerrariumGenerator generator, PropertyContainer propertyContainer) {
        this.generator = generator;
        this.propertyContainer = propertyContainer;
    }

    public static GenerationSettings deserialize(String json) {
        return GenerationSettings.deserialize(new JsonParser().parse(json).getAsJsonObject());
    }

    public static GenerationSettings deserialize(JsonObject root) {
        ResourceLocation generatorIdentifier = new ResourceLocation(JsonUtils.getString(root, "generator"));
        TerrariumGenerator generator = TerrariumGeneratorRegistry.get(generatorIdentifier);
        if (generator == null) {
            throw new JsonSyntaxException("Failed to parse generation settings, generator with id " + generatorIdentifier + " does not exist!");
        }

        JsonObject propertiesRoot = JsonUtils.getJsonObject(root, "properties");

        return new GenerationSettings(generator, PropertyContainer.deserialize(propertiesRoot));
    }

    public JsonObject serialize() {
        JsonObject root = new JsonObject();

        ResourceLocation generatorIdentifier = TerrariumGeneratorRegistry.getIdentifier(this.generator);
        if (generatorIdentifier == null) {
            throw new IllegalStateException("Attempted to serialize settings with unregistered generator!");
        }

        root.addProperty("generator", generatorIdentifier.toString());

        root.add("properties", this.propertyContainer.serialize());

        return root;
    }

    public String serializeString() {
        return new Gson().toJson(this.serialize());
    }

    public TerrariumGenerator getGenerator() {
        return this.generator;
    }

    public PropertyContainer getProperties() {
        return this.propertyContainer;
    }

    public GenerationSettings copy() {
        return GenerationSettings.deserialize(this.serialize());
    }

    public static class Default extends GenerationSettings {
        public Default() {
            super(TerrariumGeneratorRegistry.DEFAULT, new PropertyContainer(new HashMap<>(), new HashMap<>()));
        }
    }
}
