package net.gegy1000.terrarium.server.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ProgressManager;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class JsonDiscoverer<T> {
    private final Function<JsonObject, T> parser;

    public JsonDiscoverer(Function<JsonObject, T> parser) {
        this.parser = parser;
    }

    public JsonDiscoverer(Class<T> parseType) {
        this(root -> new Gson().fromJson(root, parseType));
    }

    public List<Result<T>> discoverFiles(String dataRoot, String basePath) {
        List<ModContainer> activeModList = Loader.instance().getActiveModList();
        ProgressManager.ProgressBar bar = ProgressManager.push("Discovering files", activeModList.size());

        List<Result<T>> discoveries = new ArrayList<>();

        for (ModContainer mod : activeModList) {
            bar.step(mod.getName());

            String base = dataRoot + "/" + mod.getModId() + "/" + basePath;
            CraftingHelper.findFiles(mod, base, root -> true, (root, path) -> {
                Path relativePath = root.relativize(path);
                if (!"json".equals(FilenameUtils.getExtension(path.toString())) || relativePath.getNameCount() > 1) {
                    return true;
                }

                String name = FilenameUtils.removeExtension(relativePath.toString()).replaceAll("\\\\", "/");
                ResourceLocation key = new ResourceLocation(mod.getModId(), name);

                try (BufferedReader reader = Files.newBufferedReader(path)) {
                    JsonObject rootObject = new JsonParser().parse(reader).getAsJsonObject();
                    discoveries.add(new Result<>(key, this.parser.apply(rootObject)));
                } catch (JsonParseException e) {
                    Terrarium.LOGGER.error("Couldn't parse JSON for {}", key, e);
                } catch (IOException e) {
                    Terrarium.LOGGER.error("Couldn't read JSON {} from {}", key, path, e);
                }

                return true;
            }, false, false);
        }

        ProgressManager.pop(bar);

        return discoveries;
    }

    public static class Result<T> {
        private final ResourceLocation key;
        private final T parsed;

        private Result(ResourceLocation key, T parsed) {
            this.key = key;
            this.parsed = parsed;
        }

        public ResourceLocation getKey() {
            return this.key;
        }

        public T getParsed() {
            return this.parsed;
        }
    }
}
