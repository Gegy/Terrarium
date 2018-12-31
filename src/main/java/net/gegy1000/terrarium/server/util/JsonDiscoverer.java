package net.gegy1000.terrarium.server.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public class JsonDiscoverer<T> {
    private final Function<JsonObject, T> parser;

    public JsonDiscoverer(Function<JsonObject, T> parser) {
        this.parser = parser;
    }

    public JsonDiscoverer(Class<T> parseType) {
        this(root -> new Gson().fromJson(root, parseType));
    }

    public Collection<Result<T>> discoverFiles(ResourceManager resourceManager, String basePath) {
        Collection<Result<T>> discoveries = new ArrayList<>();

        for (Identifier location : resourceManager.findResources(basePath, s -> s.endsWith(".json"))) {
            try (Resource resource = resourceManager.getResource(location)) {
                Identifier key = this.resolveKey(basePath, location);
                BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
                JsonObject rootObject = new JsonParser().parse(reader).getAsJsonObject();
                discoveries.add(new Result<>(key, this.parser.apply(rootObject)));
            } catch (JsonParseException e) {
                Terrarium.LOGGER.error("Couldn't parse JSON for {}", location, e);
            } catch (IOException e) {
                Terrarium.LOGGER.error("Couldn't read JSON for {}", location, e);
            }
        }

        return discoveries;
    }

    private Identifier resolveKey(String basePath, Identifier location) {
        Path path = Paths.get(basePath).relativize(Paths.get(location.getPath()));
        return new Identifier(location.getNamespace(), FilenameUtils.removeExtension(path.toString()));
    }

    public static class Result<T> {
        private final Identifier key;
        private final T parsed;

        private Result(Identifier key, T parsed) {
            this.key = key;
            this.parsed = parsed;
        }

        public Identifier getKey() {
            return this.key;
        }

        public T getParsed() {
            return this.parsed;
        }
    }
}
