package dev.gegy.terrarium.backend.maxent;

import dev.gegy.terrarium.backend.expr.predictor.Predictor;
import dev.gegy.terrarium.backend.expr.predictor.PredictorNode;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class MaxentLambdas<T> {
    private final List<Feature<T>> features;
    private final Object2FloatMap<String> fields;

    private MaxentLambdas(final List<Feature<T>> features, final Object2FloatMap<String> fields) {
        this.features = features;
        this.fields = fields;
    }

    public static <T> MaxentLambdas<T> parse(final Reader reader, final Function<String, Optional<Predictor<T>>> namedParameters) throws IOException, MaxentParseException {
        final List<Feature<T>> features = new ArrayList<>();
        final Object2FloatMap<String> fields = new Object2FloatOpenHashMap<>();

        final FeatureParser<T> featureParser = new FeatureParser<>(name -> namedParameters.apply(name).map(Feature.Parameter::new));

        final BufferedReader bufferedReader = new BufferedReader(reader);

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            final String[] tokens = line.split(", ");
            if (tokens.length == 2) {
                try {
                    final String key = tokens[0];
                    final float value = Float.parseFloat(tokens[1]);
                    fields.put(key, value);
                } catch (final NumberFormatException e) {
                    throw new MaxentParseException(e);
                }
            } else if (tokens.length == 4) {
                final Feature<T> feature = featureParser.parse(tokens);
                if (!feature.equals(Feature.Const.zero())) {
                    features.add(feature);
                }
            } else {
                throw new MaxentParseException("Malformed line: " + line);
            }
        }

        return new MaxentLambdas<>(List.copyOf(features), Object2FloatMaps.unmodifiable(fields));
    }

    public Predictor<T> buildPredictor() {
        return buildPredictor(MaxentOutputType.CLOGLOG);
    }

    public Predictor<T> buildPredictor(final MaxentOutputType outputType) {
        final float linearPredictorNormalizer = fields.getOrDefault("linearPredictorNormalizer", 0.0f);
        final float densityNormalizer = fields.getOrDefault("densityNormalizer", 1.0f);
        final float entropy = fields.getOrDefault("entropy", 0.0f);
        final Feature.Output<T> output = new Feature.Output<>(features, linearPredictorNormalizer, densityNormalizer, entropy, outputType);
        return PredictorNode.compile(output.toPredictor());
    }
}
