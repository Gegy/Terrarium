package net.gegy1000.terrarium.server.world.json;

import com.google.gson.JsonObject;

public interface ValueProvider<T> {
    T provide(JsonValueParser valueParser, JsonObject root) throws InvalidJsonException;
}
