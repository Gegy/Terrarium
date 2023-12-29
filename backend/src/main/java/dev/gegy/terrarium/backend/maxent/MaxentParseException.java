package dev.gegy.terrarium.backend.maxent;

public class MaxentParseException extends Exception {
    public MaxentParseException() {
    }

    public MaxentParseException(final String message) {
        super(message);
    }

    public MaxentParseException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public MaxentParseException(final Throwable cause) {
        super(cause);
    }
}
