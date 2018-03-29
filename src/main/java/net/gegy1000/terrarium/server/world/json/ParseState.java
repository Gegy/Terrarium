package net.gegy1000.terrarium.server.world.json;

import net.gegy1000.terrarium.Terrarium;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ParseState {
    private final LinkedList<Context> parseContext = new LinkedList<>();
    private final LinkedList<Error> errors = new LinkedList<>();

    protected void finish(String action) {
        if (!this.errors.isEmpty()) {
            Terrarium.LOGGER.error("Detected {} errors while trying to {}", this.errors.size(), action);

            Map<String, List<Error>> grouped = this.errors.stream().collect(Collectors.groupingBy(Error::getLocation));
            for (Map.Entry<String, List<Error>> locationErrors : grouped.entrySet()) {
                List<Error> errors = locationErrors.getValue();
                Terrarium.LOGGER.error("  {} errors in {}", errors.size(), locationErrors.getKey());
                for (Error error : errors) {
                    if (error.action != null) {
                        Terrarium.LOGGER.error("    {} while {}", error.message, error.action);
                    } else {
                        Terrarium.LOGGER.error("    {}", error.message);
                    }
                }
                Terrarium.LOGGER.error("");
            }
        }

        this.parseContext.clear();
        this.errors.clear();
    }

    public void pushContext(ResourceLocation jsonLocation, String action) {
        this.parseContext.add(new Context(jsonLocation, action));
    }

    public void popContext() {
        this.parseContext.removeLast();
    }

    public Error createError(String message) {
        return new Error(message, this.getCurrentLocationString(), this.getCurrentAction());
    }

    public void error(Error error) {
        this.errors.add(error);
    }

    public String getCurrentLocationString() {
        ResourceLocation currentLocation = this.getCurrentLocation();
        return currentLocation == null ? "root" : currentLocation.toString();
    }

    @Nullable
    public ResourceLocation getCurrentLocation() {
        Iterator<Context> contextIterator = this.parseContext.descendingIterator();
        while (contextIterator.hasNext()) {
            Context context = contextIterator.next();
            if (context.jsonLocation != null) {
                return context.jsonLocation;
            }
        }
        return null;
    }

    public String getCurrentAction() {
        Iterator<Context> contextIterator = this.parseContext.descendingIterator();
        while (contextIterator.hasNext()) {
            Context context = contextIterator.next();
            if (context.action != null) {
                return context.action;
            }
        }
        return null;
    }

    public static class Context {
        private final ResourceLocation jsonLocation;
        private final String action;

        private Context(@Nullable ResourceLocation jsonLocation, @Nullable String action) {
            this.jsonLocation = jsonLocation;
            this.action = action;
        }

        @Nullable
        public ResourceLocation getJsonLocation() {
            return this.jsonLocation;
        }

        @Nullable
        public String getAction() {
            return this.action;
        }
    }

    public static class Error {
        private final String message;

        private final String location;
        private final String action;

        private Error(String message, String location, @Nullable String action) {
            this.message = message;
            this.location = location;
            this.action = action;
        }

        public String getMessage() {
            return this.message;
        }

        public String getLocation() {
            return this.location;
        }

        @Nullable
        public String getAction() {
            return this.action;
        }
    }
}
