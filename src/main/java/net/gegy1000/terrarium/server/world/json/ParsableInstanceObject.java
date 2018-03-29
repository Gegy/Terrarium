package net.gegy1000.terrarium.server.world.json;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ParsableInstanceObject<T> {
    private final String description;
    private final ResourceLocation location;
    private final InstanceObjectParser<T> parser;
    private final JsonObject objectRoot;

    public ParsableInstanceObject(String description, ResourceLocation location, InstanceObjectParser<T> parser, JsonObject objectRoot) {
        this.description = description;
        this.parser = parser;
        this.objectRoot = objectRoot;
        this.location = location;
    }

    public T parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser) throws InvalidJsonException {
        try {
            ParseStateHandler.pushContext(this.location, "parsing " + this.description);

            if (this.objectRoot.has("apply_if")) {
                if (!valueParser.parseBoolean(this.objectRoot, "apply_if")) {
                    return null;
                }
            }
            return this.parser.parse(worldData, world, valueParser, this.objectRoot);
        } finally {
            ParseStateHandler.popContext();
        }
    }
}
