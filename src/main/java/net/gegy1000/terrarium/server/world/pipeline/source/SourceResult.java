package net.gegy1000.terrarium.server.world.pipeline.source;

import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;

import javax.annotation.Nullable;

public class SourceResult<T extends TiledDataAccess> {
    private final T value;
    private final Error error;
    private final String errorCause;

    private SourceResult(T value, Error error, String errorCause) {
        this.value = value;
        this.error = error;
        this.errorCause = errorCause;
    }

    public static <T extends TiledDataAccess> SourceResult<T> success(T value) {
        return new SourceResult<>(value, null, null);
    }

    public static <T extends TiledDataAccess> SourceResult<T> empty() {
        return new SourceResult<>(null, null, null);
    }

    public static <T extends TiledDataAccess> SourceResult<T> malformed(String cause) {
        return new SourceResult<>(null, Error.MALFORMED, cause);
    }

    public static <T extends TiledDataAccess> SourceResult<T> exception(Exception cause) {
        String causeMessage = cause.getMessage();
        String message;
        if (causeMessage != null) {
            message = String.format("(%s: %s)", cause.getClass().getSimpleName(), causeMessage);
        } else {
            message = String.format("(%s)", cause.getClass().getSimpleName());
        }
        return new SourceResult<>(null, Error.EXCEPTION, message);
    }

    public boolean isSuccess() {
        return this.error == null;
    }

    public boolean isError() {
        return this.error != null;
    }

    @Nullable
    public T getValue() {
        return this.value;
    }

    @Nullable
    public Error getError() {
        return this.error;
    }

    @Nullable
    public String getErrorCause() {
        return this.errorCause;
    }

    public enum Error {
        MALFORMED,
        EXCEPTION
    }
}
