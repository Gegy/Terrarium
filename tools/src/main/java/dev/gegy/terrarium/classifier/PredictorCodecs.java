package dev.gegy.terrarium.classifier;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.gegy.terrarium.Biome;
import dev.gegy.terrarium.backend.earth.GeoParameters;
import dev.gegy.terrarium.backend.expr.classifier.ClassifierNode;
import dev.gegy.terrarium.backend.expr.predictor.Predictor;
import dev.gegy.terrarium.backend.expr.predictor.PredictorNode;

public class PredictorCodecs {
    public static final Codec<PredictorNode<GeoParameters>> PREDICTOR = PredictorNode.createCodecs(
            createBuiltinPredictorCodec(),
            directCodec -> directCodec
    ).externalCodec();

    public static final Codec<ClassifierNode<GeoParameters, Biome>> BIOME_CLASSIFIER = createClassifierCodec(Biome.CODEC);

    private static <T> Codec<ClassifierNode<GeoParameters, T>> createClassifierCodec(final Codec<T> valueCodec) {
        return ClassifierNode.createCodec(PREDICTOR, valueCodec, directCodec -> directCodec);
    }

    private static Codec<Predictor<GeoParameters>> createBuiltinPredictorCodec() {
        final BiMap<String, Predictor<GeoParameters>> predictors = HashBiMap.create();
        GeoParameters.forEachFeature((id, predictor) -> predictors.put("terrarium:" + id, predictor));
        return Codec.STRING.flatXmap(
                id -> {
                    final Predictor<GeoParameters> predictor = predictors.get(id);
                    return predictor != null ? DataResult.success(predictor) : DataResult.error(() -> "No built-in feature with id: " + id);
                },
                predictor -> {
                    final String id = predictors.inverse().get(predictor);
                    return id != null ? DataResult.success(id) : DataResult.error(() -> "Not a built-in feature");
                }
        );
    }
}
