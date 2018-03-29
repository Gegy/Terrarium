package net.gegy1000.terrarium.server.world.generator.customization;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.gegy1000.terrarium.server.world.generator.TerrariumGenerator;
import net.gegy1000.terrarium.server.world.generator.TerrariumGeneratorRegistry;
import net.gegy1000.terrarium.server.world.json.InvalidJsonException;
import net.gegy1000.terrarium.server.world.json.ParseStateHandler;
import net.gegy1000.terrarium.server.world.json.ParseUtils;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

public class GenerationSettings {
    private final TerrariumGenerator generator;
    private final PropertyContainer propertyContainer;

    private GenerationSettings(TerrariumGenerator generator, PropertyContainer propertyContainer) {
        this.generator = generator;
        this.propertyContainer = propertyContainer;
    }

    public static GenerationSettings deserialize(String json) throws InvalidJsonException {
        return GenerationSettings.deserialize(new JsonParser().parse(json).getAsJsonObject());
    }

    public static GenerationSettings deserialize(JsonObject root) throws InvalidJsonException {
        ParseStateHandler.begin();

        try {
            ParseStateHandler.pushContext("parsing generation settings");

            ResourceLocation generatorIdentifier = new ResourceLocation(ParseUtils.getString(root, "generator"));
            TerrariumGenerator generator = TerrariumGeneratorRegistry.get(generatorIdentifier);
            if (generator == null) {
                throw new InvalidJsonException("Failed to parse generation settings, generator with id \"" + generatorIdentifier + "\" does not exist!");
            }

            PropertyContainer properties = ParseUtils.parseObject(root, "properties", PropertyContainer::deserialize);

            ParseStateHandler.popContext();

            return new GenerationSettings(generator, properties);
        } finally {
            ParseStateHandler.finish("parse settings");
        }
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
        try {
            return GenerationSettings.deserialize(this.serialize());
        } catch (InvalidJsonException e) {
            throw new IllegalStateException("Failed to parsed copied settings", e);
        }
    }

    public static class Default extends GenerationSettings {
        public Default() {
            super(TerrariumGeneratorRegistry.DEFAULT, new PropertyContainer(new HashMap<>(), new HashMap<>()));
        }
    }
}
