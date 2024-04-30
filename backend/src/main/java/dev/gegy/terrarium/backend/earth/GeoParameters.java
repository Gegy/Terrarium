package dev.gegy.terrarium.backend.earth;

import dev.gegy.terrarium.backend.earth.cover.Cover;
import dev.gegy.terrarium.backend.expr.predictor.Predictor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GeoParameters {
    public static final Predictor<GeoParameters> ELEVATION = p -> p.elevation;
    public static final Map<Cover, Predictor<GeoParameters>> IS_COVER = Arrays.stream(Cover.values()).collect(Collectors.toMap(
            Function.identity(),
            cover -> parameters -> parameters.cover == cover ? 1.0f : 0.0f
    ));

    private float elevation;
    private Cover cover = Cover.NONE;

    public static void forEach(final BiConsumer<String, Predictor<GeoParameters>> consumer) {
        consumer.accept("elevation", ELEVATION);
        IS_COVER.forEach((cover, predictor) -> consumer.accept("is_cover/" + cover.getName(), predictor));
    }

    public GeoParameters set(final float elevation, final Cover cover) {
        this.elevation = elevation;
        this.cover = cover;
        return this;
    }
}
