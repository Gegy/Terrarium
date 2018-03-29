package net.gegy1000.terrarium.server.world.json;

import net.minecraft.util.ResourceLocation;

import java.util.LinkedList;

public class ParseStateHandler {
    private static final LinkedList<ParseState> STATES = new LinkedList<>();

    public static void begin() {
        ParseState state = new ParseState();
        STATES.add(state);
    }

    public static void finish(String action) {
        ParseState state = STATES.removeLast();
        state.finish(action);
    }

    public static ParseState get() {
        if (STATES.isEmpty()) {
            throw new IllegalStateException("No state active");
        }
        return STATES.getLast();
    }

    public static void pushContext(ResourceLocation jsonLocation, String action) {
        ParseStateHandler.get().pushContext(jsonLocation, action);
    }

    public static void pushContext(String action) {
        ParseStateHandler.pushContext(null, action);
    }

    public static void pushContext(ResourceLocation jsonLocation) {
        ParseStateHandler.pushContext(jsonLocation, null);
    }

    public static void popContext() {
        ParseStateHandler.get().popContext();
    }

    public static ParseState.Error error(String message) {
        ParseState state = ParseStateHandler.get();
        ParseState.Error error = state.createError(message);
        state.error(error);
        return error;
    }

    public static void error(InvalidJsonException e) {
        ParseStateHandler.get().error(e.getError());
    }
}
