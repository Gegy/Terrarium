package net.gegy1000.earth.server.shared;

public final class SharedInitException extends Exception {
    public SharedInitException(String message) {
        super(message);
    }

    public SharedInitException(Throwable cause) {
        super(cause);
    }

    public SharedInitException(String message, Throwable cause) {
        super(message, cause);
    }
}
