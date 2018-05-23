package net.gegy1000.terrarium.server.world.cover;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InvalidJsonException;
import net.gegy1000.terrarium.server.world.json.ParseUtils;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class DeclaredCoverTypeParser {
    public static void parseCoverTypes(JsonObject objectRoot, InstanceJsonValueParser valueParser, Handler handler) throws InvalidJsonException {
        ParseUtils.handleObject(objectRoot, "cover_types", coverTypeRoot -> {
            CoverGenerationContext defaultContext;
            if (coverTypeRoot.has("default_context")) {
                defaultContext = valueParser.parseContext(coverTypeRoot, "default_context");
            } else {
                defaultContext = null;
            }

            ParseUtils.handleArray(coverTypeRoot, "types", coverTypeArray -> {
                for (JsonElement element : coverTypeArray) {
                    if (element.isJsonObject()) {
                        JsonObject coverObject = element.getAsJsonObject();
                        ResourceLocation coverKey = new ResourceLocation(ParseUtils.getString(coverObject, "type"));
                        CoverGenerationContext context = valueParser.parseContext(coverObject, "context");
                        DeclaredCoverTypeParser.handle(coverKey, context, handler);
                    } else if (ParseUtils.isString(element)) {
                        if (defaultContext == null) {
                            throw new InvalidJsonException("Cannot declare defaulted cover type with no default context!");
                        }
                        ResourceLocation coverKey = new ResourceLocation(element.getAsString());
                        DeclaredCoverTypeParser.handle(coverKey, defaultContext, handler);
                    } else {
                        Terrarium.LOGGER.warn("Ignored invalid cover type entry {}", element);
                    }
                }
            });
        });
    }

    public static CoverType[] parseCoverTypes(JsonObject objectRoot, InstanceJsonValueParser valueParser) throws InvalidJsonException {
        List<CoverType> coverTypes = new ArrayList<>();
        DeclaredCoverTypeParser.parseCoverTypes(objectRoot, valueParser, new Handler() {
            @Override
            public <T extends CoverGenerationContext> void handle(CoverType<T> coverType, T context) {
                coverTypes.add(coverType);
            }
        });
        return coverTypes.toArray(new CoverType[0]);
    }

    private static void handle(ResourceLocation coverKey, CoverGenerationContext context, Handler handler) throws InvalidJsonException {
        CoverType<?> coverType = CoverRegistry.getCoverType(coverKey);
        if (coverType == null) {
            throw new InvalidJsonException("Found unregistered cover type " + coverKey);
        }
        DeclaredCoverTypeParser.handle(coverType, context, handler);
    }

    @SuppressWarnings("unchecked")
    private static <T extends CoverGenerationContext> void handle(CoverType<T> coverType, CoverGenerationContext context, Handler handler) throws InvalidJsonException {
        if (coverType.getRequiredContext().isAssignableFrom(context.getClass())) {
            handler.handle(coverType, (T) context);
        } else {
            throw new InvalidJsonException("Tried to apply context of wrong type to " + coverType);
        }
    }

    public interface Handler {
        <T extends CoverGenerationContext> void handle(CoverType<T> coverType, T context);
    }
}
