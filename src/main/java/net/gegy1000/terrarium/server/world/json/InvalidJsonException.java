package net.gegy1000.terrarium.server.world.json;

public class InvalidJsonException extends Exception {
    private final ParseState.Error error;

    public InvalidJsonException(String message) {
        super(message);
        this.error = ParseStateHandler.get().createError(message);
    }

    public ParseState.Error getError() {
        return this.error;
    }
}
