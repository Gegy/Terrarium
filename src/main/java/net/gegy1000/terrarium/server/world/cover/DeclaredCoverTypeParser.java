package net.gegy1000.terrarium.server.world.cover;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.TerrariumJsonUtils;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

public class DeclaredCoverTypeParser {
    public static void parseCoverTypes(JsonObject objectRoot, InstanceJsonValueParser valueParser, Handler handler) {
        JsonObject coverTypeRoot = TerrariumJsonUtils.parseRemoteObject(objectRoot, "cover_types");

        CoverGenerationContext defaultContext = null;
        if (coverTypeRoot.has("default_context")) {
            defaultContext = valueParser.parseContext(coverTypeRoot, "default_context");
        }

        JsonArray coverTypeArray = TerrariumJsonUtils.parseRemoteArray(coverTypeRoot, "types");
        for (JsonElement element : coverTypeArray) {
            if (element.isJsonObject()) {
                JsonObject coverObject = element.getAsJsonObject();
                ResourceLocation coverKey = new ResourceLocation(JsonUtils.getString(coverObject, "type"));
                CoverGenerationContext context = valueParser.parseContext(coverObject, "context");
                DeclaredCoverTypeParser.handle(coverKey, context, handler);
            } else if (TerrariumJsonUtils.isString(element)) {
                if (defaultContext == null) {
                    throw new JsonSyntaxException("Cannot declare defaulted cover type with no default context!");
                }
                ResourceLocation coverKey = new ResourceLocation(element.getAsString());
                DeclaredCoverTypeParser.handle(coverKey, defaultContext, handler);
            } else {
                Terrarium.LOGGER.warn("Ignored invalid cover type entry {}", element);
            }
        }
    }

    private static void handle(ResourceLocation coverKey, CoverGenerationContext context, Handler handler) {
        CoverType<?> coverType = CoverRegistry.getCoverType(coverKey);
        if (coverType == null) {
            throw new JsonSyntaxException("Found unregistered cover type " + coverKey);
        }
        DeclaredCoverTypeParser.handle(coverType, context, handler);
    }

    @SuppressWarnings("unchecked")
    private static <T extends CoverGenerationContext> void handle(CoverType<T> coverType, CoverGenerationContext context, Handler handler) {
        if (coverType.getRequiredContext().isAssignableFrom(context.getClass())) {
            handler.handle(coverType, (T) context);
        } else {
            throw new JsonSyntaxException("Tried to apply context of wrong type to " + coverType);
        }
    }

    public interface Handler {
        <T extends CoverGenerationContext> void handle(CoverType<T> coverType, T context);
    }
}
