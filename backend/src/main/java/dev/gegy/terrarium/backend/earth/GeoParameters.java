package dev.gegy.terrarium.backend.earth;

import dev.gegy.terrarium.backend.expr.predictor.Predictor;

import java.util.function.BiConsumer;

public class GeoParameters {
    public static final Predictor<GeoParameters> ELEVATION = p -> p.elevation;

    private float elevation;

    public static void forEach(final BiConsumer<String, Predictor<GeoParameters>> consumer) {
        consumer.accept("elevation", ELEVATION);
    }

    public GeoParameters set(final float elevation) {
        this.elevation = elevation;
        return this;
    }
}
