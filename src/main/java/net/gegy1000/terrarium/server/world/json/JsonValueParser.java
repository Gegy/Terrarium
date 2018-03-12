package net.gegy1000.terrarium.server.world.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public interface JsonValueParser {
    boolean parseBoolean(JsonObject objectRoot, String key);

    double parseDouble(JsonObject objectRoot, String key);

    int parseInteger(JsonObject objectRoot, String key);

    String parseString(JsonObject objectRoot, String key);

    IBlockState parseBlockState(JsonObject objectRoot, String key);

    @SuppressWarnings("unchecked")
    default <T> T parseFromProvider(JsonElement element) {
        if (element != null && element.isJsonObject()) {
            JsonObject root = element.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                if (entry.getValue().isJsonObject()) {
                    ResourceLocation providerKey = new ResourceLocation(entry.getKey());
                    ValueProvider<?> provider = ValueProviderRegistry.get(providerKey);

                    if (provider != null) {
                        try {
                            return (T) provider.provide(this, entry.getValue().getAsJsonObject());
                        } catch (ClassCastException e) {
                            throw new JsonSyntaxException("Got value provider " + providerKey + " of wrong type");
                        }
                    }
                }
            }
        }

        return null;
    }
}
