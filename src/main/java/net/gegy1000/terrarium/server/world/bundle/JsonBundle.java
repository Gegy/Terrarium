package net.gegy1000.terrarium.server.world.bundle;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.json.TerrariumJsonUtils;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JsonBundle implements IdBundle {
    private final String identifier;
    private final ImmutableList<ResourceLocation> entries;

    private JsonBundle(String identifier, List<ResourceLocation> entries) {
        this.identifier = identifier;
        this.entries = ImmutableList.copyOf(entries);
    }

    public static JsonBundle parse(JsonObject root) {
        String identifier = JsonUtils.getString(root, "identifier");

        List<ResourceLocation> entries = new ArrayList<>();
        JsonArray entryArray = TerrariumJsonUtils.parseRemoteArray(root, "entries");

        for (JsonElement entryElement : entryArray) {
            if (entryElement.isJsonPrimitive() && entryElement.getAsJsonPrimitive().isString()) {
                entries.add(new ResourceLocation(entryElement.getAsString()));
            } else {
                Terrarium.LOGGER.warn("Ignored invalid bundle entry {}", entryElement);
            }
        }

        return new JsonBundle(identifier, entries);
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public Collection<ResourceLocation> getEntries() {
        return this.entries;
    }
}
