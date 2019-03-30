package net.gegy1000.terrarium.server.world.pipeline;

public class GenerationCancelledException extends RuntimeException {
    public GenerationCancelledException(Throwable cause) {
        super(cause);
    }

    public static void propagate(Throwable throwable) {
        if (throwable instanceof GenerationCancelledException) {
            throw (GenerationCancelledException) throwable;
        }

        Throwable cause = throwable.getCause();
        if (cause != null) {
            propagate(cause);
        }
    }
}
