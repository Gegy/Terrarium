package net.gegy1000.terrarium.server.world.pipeline.source;

public class NoDataException extends SourceException {
    public NoDataException(String message, Exception cause) {
        super(message, cause);
    }

    public NoDataException(String message) {
        super(message, null);
    }
}
