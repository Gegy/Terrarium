package net.gegy1000.terrarium.server.world.json;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.minecraft.world.World;

public class ParsableInstanceObject<T> {
    private final InstanceObjectParser<T> parser;
    private final JsonObject objectRoot;

    public ParsableInstanceObject(InstanceObjectParser<T> parser, JsonObject objectRoot) {
        this.parser = parser;
        this.objectRoot = objectRoot;
    }

    public T parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser) {
        if (this.objectRoot.has("apply_if")) {
            if (!valueParser.parseBoolean(this.objectRoot, "apply_if")) {
                return null;
            }
        }
        return this.parser.parse(worldData, world, valueParser, this.objectRoot);
    }
}
