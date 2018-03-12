package net.gegy1000.terrarium.server.world.json;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.minecraft.world.World;

public interface InstanceObjectParser<T> {
    T parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot);
}
