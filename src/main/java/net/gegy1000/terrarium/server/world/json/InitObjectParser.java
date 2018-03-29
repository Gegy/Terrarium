package net.gegy1000.terrarium.server.world.json;

import com.google.gson.JsonObject;

public interface InitObjectParser<T> {
    T parse(JsonValueParser valueParser, JsonObject objectRoot) throws InvalidJsonException;
}
