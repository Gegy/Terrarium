package net.gegy1000.terrarium.server.world.json;

import com.google.common.base.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ParseUtils {
    private static final JsonParser PARSER = new JsonParser();

    public static void handleObject(JsonObject root, String key, Handler<JsonObject> handler) throws InvalidJsonException {
        ParseUtils.parseObject(root, key, new Handler.Wrapper<>(handler));
    }

    public static <T> T parseObject(JsonObject root, String key, Parser<JsonObject, T> handler) throws InvalidJsonException {
        JsonElement element = root.get(key);
        if (element != null) {
            T result;
            ParseStateHandler.pushContext("parsing \"" + key + "\" in " + ParseStateHandler.get().getCurrentLocationString());
            if (!element.isJsonObject()) {
                result = ParseUtils.parseElement(element, remoteElement -> {
                    if (remoteElement == null || !remoteElement.isJsonObject()) {
                        throw new InvalidJsonException("Found invalid remote object");
                    }
                    return handler.parse(remoteElement.getAsJsonObject());
                });
            } else {
                result = handler.parse(element.getAsJsonObject());
            }
            ParseStateHandler.popContext();
            return result;
        }
        throw new InvalidJsonException("Unable to find object for \"" + key + "\" in " + ParseStateHandler.get().getCurrentLocationString());
    }

    public static void handleArray(JsonObject root, String key, Handler<JsonArray> handler) throws InvalidJsonException {
        ParseUtils.parseArray(root, key, new Handler.Wrapper<>(handler));
    }

    public static <T> T parseArray(JsonObject root, String key, Parser<JsonArray, T> handler) throws InvalidJsonException {
        JsonElement element = root.get(key);
        if (element != null) {
            T result;
            ParseStateHandler.pushContext("parsing \"" + key + "\" in " + ParseStateHandler.get().getCurrentLocationString());
            if (!element.isJsonArray()) {
                result = ParseUtils.parseElement(element, remoteElement -> {
                    if (remoteElement == null || !remoteElement.isJsonArray()) {
                        throw new InvalidJsonException("Found invalid remote array");
                    }
                    return handler.parse(remoteElement.getAsJsonArray());
                });
            } else {
                result = handler.parse(element.getAsJsonArray());
            }
            ParseStateHandler.popContext();
            return result;
        }
        throw new InvalidJsonException("Unable to find array for \"" + key + "\" in " + ParseStateHandler.get().getCurrentLocationString());
    }

    private static <T> T parseElement(JsonElement element, Parser<JsonElement, T> handler) throws InvalidJsonException {
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) {
                ResourceLocation remoteKey = new ResourceLocation(primitive.getAsString());
                String path = "/data/" + remoteKey.getResourceDomain() + "/" + remoteKey.getResourcePath();
                if (!path.endsWith(".json")) {
                    path += ".json";
                }
                ParseStateHandler.pushContext(remoteKey);
                T result;
                // TODO: Will this work across mods? I suspect it'll only load if the JSON is within the Terrarium jar
                try (InputStream input = Terrarium.class.getResourceAsStream(path)) {
                    if (input == null) {
                        throw new InvalidJsonException("Remote element at " + path + " does not exist!");
                    }
                    result = handler.parse(PARSER.parse(new InputStreamReader(input)));
                } catch (IOException e) {
                    throw new InvalidJsonException("Failed to load remote element: " + e.getMessage());
                }
                ParseStateHandler.popContext();
                return result;
            }
        }
        throw new InvalidJsonException("Found invalid remote element");
    }

    public static IBlockState parseBlockState(String blockstateKey) throws InvalidJsonException {
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
                throw new InvalidJsonException("Invalid property syntax for \"" + listedProperty + "\"");
            }
            String propertyName = propertyParts[0];
            String propertyValue = propertyParts[1];

            IProperty<?> property = ParseUtils.getProperty(state, propertyName);
            state = ParseUtils.setProperty(state, property, propertyValue);
        }

        return state;
    }

    private static IProperty<?> getProperty(IBlockState state, String propertyName) throws InvalidJsonException {
        for (IProperty<?> property : state.getPropertyKeys()) {
            if (property.getName().equals(propertyName)) {
                return property;
            }
        }
        throw new InvalidJsonException("Found no property with name \"" + propertyName + "\" on block \"" + state.getBlock() + "\"");
    }

    private static <T extends Comparable<T>> IBlockState setProperty(IBlockState state, IProperty<T> property, String value) throws InvalidJsonException {
        Optional<T> parsed = property.parseValue(value);
        if (!parsed.isPresent()) {
            throw new InvalidJsonException("Invalid value \"" + value + "\" for property \"" + property.getName() + "\"");
        }
        return state.withProperty(property, parsed.get());
    }

    public static String getString(JsonObject root, String key) throws InvalidJsonException {
        JsonElement element = root.get(key);
        if (element == null || !ParseUtils.isString(element)) {
            throw new InvalidJsonException("Expected string with key \"" + key + "\"");
        }
        return root.get(key).getAsString();
    }

    public static int getInt(JsonObject root, String key) throws InvalidJsonException {
        JsonElement element = root.get(key);
        if (element == null || !ParseUtils.isNumber(element)) {
            throw new InvalidJsonException("Expected int with key \"" + key + "\"");
        }
        return root.get(key).getAsInt();
    }

    public static float getFloat(JsonObject root, String key) throws InvalidJsonException {
        JsonElement element = root.get(key);
        if (element == null || !ParseUtils.isNumber(element)) {
            throw new InvalidJsonException("Expected float with key \"" + key + "\"");
        }
        return root.get(key).getAsFloat();
    }

    public static JsonArray getJsonArray(JsonObject root, String key) throws InvalidJsonException {
        JsonElement element = root.get(key);
        if (element == null || !element.isJsonArray()) {
            throw new InvalidJsonException("Expected array with key \"" + key + "\"");
        }
        return root.get(key).getAsJsonArray();
    }

    public static boolean isString(JsonElement element) {
        return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();
    }

    public static boolean isNumber(JsonElement element) {
        return element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber();
    }

    public interface Parser<T, R> {
        R parse(T input) throws InvalidJsonException;
    }

    public interface Handler<T> {
        void handle(T input) throws InvalidJsonException;

        class Wrapper<T> implements Parser<T, Void> {
            private final Handler<T> handler;

            public Wrapper(Handler<T> handler) {
                this.handler = handler;
            }

            @Override
            public Void parse(T input) throws InvalidJsonException {
                this.handler.handle(input);
                return null;
            }
        }
    }
}
