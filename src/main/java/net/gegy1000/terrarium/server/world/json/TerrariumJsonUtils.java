package net.gegy1000.terrarium.server.world.json;

import com.google.common.base.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TerrariumJsonUtils {
    private static final JsonParser PARSER = new JsonParser();

    public static JsonObject parseRemoteObject(JsonObject root, String key) {
        JsonElement element = root.get(key);
        if (element != null && !element.isJsonObject()) {
            JsonElement remoteElement = TerrariumJsonUtils.parseRemoteElement(element);
            if (remoteElement != null) {
                element = remoteElement;
            }
        }
        if (element != null && element.isJsonObject()) {
            return element.getAsJsonObject();
        }
        throw new JsonSyntaxException("Could not find local or remote JsonObject for " + key + " on " + root);
    }

    public static JsonArray parseRemoteArray(JsonObject root, String key) {
        JsonElement element = root.get(key);
        if (!element.isJsonArray()) {
            JsonElement remoteElement = TerrariumJsonUtils.parseRemoteElement(element);
            if (remoteElement != null) {
                element = remoteElement;
            }
        }
        if (element.isJsonArray()) {
            return element.getAsJsonArray();
        }
        throw new JsonSyntaxException("Could not find local or remote JsonArray for " + key + " on " + root);
    }

    private static JsonElement parseRemoteElement(JsonElement element) {
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) {
                ResourceLocation remoteKey = new ResourceLocation(primitive.getAsString());
                String path = "/data/" + remoteKey.getResourceDomain() + "/" + remoteKey.getResourcePath();
                if (!path.endsWith(".json")) {
                    path += ".json";
                }
                // TODO: Will this work across mods? I suspect it'll only load if the JSON is within the Terrarium jar
                try (InputStream input = Terrarium.class.getResourceAsStream(path)) {
                    return PARSER.parse(new InputStreamReader(input));
                } catch (IOException e) {
                    throw new JsonSyntaxException("Failed to load remote JsonElement at " + path, e);
                }
            }
        }
        return null;
    }

    public static IBlockState parseBlockState(String blockstateKey) {
        String[] parts = blockstateKey.split("#");

        Block block = Block.REGISTRY.getObject(new ResourceLocation(parts[0]));
        if (parts.length == 1) {
            return block.getDefaultState();
        }

        String[] listedPropertyParts = parts[1].split(",");

        IBlockState state = block.getDefaultState();
        for (String listedProperty : listedPropertyParts) {
            String[] propertyParts = listedProperty.split("=");
            if (propertyParts.length != 2) {
                throw new JsonSyntaxException("Invalid property syntax for " + listedProperty);
            }
            String propertyName = propertyParts[0];
            String propertyValue = propertyParts[1];

            IProperty<?> property = TerrariumJsonUtils.getProperty(state, propertyName);
            state = TerrariumJsonUtils.setProperty(state, property, propertyValue);
        }

        return state;
    }

    private static IProperty<?> getProperty(IBlockState state, String propertyName) {
        for (IProperty<?> property : state.getPropertyKeys()) {
            if (property.getName().equals(propertyName)) {
                return property;
            }
        }
        throw new JsonSyntaxException("Found no property with name " + propertyName + " on block " + state.getBlock());
    }

    private static <T extends Comparable<T>> IBlockState setProperty(IBlockState state, IProperty<T> property, String value) {
        Optional<T> parsed = property.parseValue(value);
        if (!parsed.isPresent()) {
            throw new JsonSyntaxException("Invalid value " + value + " for property " + property.getName());
        }
        return state.withProperty(property, parsed.get());
    }

    public static boolean isString(JsonElement element) {
        return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();
    }
}
