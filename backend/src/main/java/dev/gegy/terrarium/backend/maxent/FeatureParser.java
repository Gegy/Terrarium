package dev.gegy.terrarium.backend.maxent;

import java.util.Optional;
import java.util.function.Function;

public class FeatureParser<T> {
    private static final float EPSILON = 0.01f;

    private final Function<String, Optional<Feature<T>>> namedInputs;

    public FeatureParser(final Function<String, Optional<Feature<T>>> namedInputs) {
        this.namedInputs = namedInputs;
    }

    public Feature<T> parse(final String[] tokens) throws MaxentParseException {
        if (tokens.length != 4) {
            throw new MaxentParseException("Invalid number of tokens for feature");
        }

        try {
            final String ident = tokens[0];

            final float lambda = Float.parseFloat(tokens[1]);
            if (Math.abs(lambda) <= EPSILON) {
                return Feature.Const.zero();
            }

            final float min = Float.parseFloat(tokens[2]);
            final float max = Float.parseFloat(tokens[3]);

            if (ident.startsWith("(") && ident.endsWith(")")) {
                final String expression = ident.substring(1, ident.length() - 1);
                return parseExpression(expression, lambda, min, max);
            } else if (ident.startsWith("'")) {
                final Feature<T> feature = getInput(ident.substring(1));
                return new Feature.Hinge<>(feature, lambda, min, max);
            } else if (ident.startsWith("`")) {
                final Feature<T> feature = getInput(ident.substring(1));
                return new Feature.ReverseHinge<>(feature, lambda, min, max);
            } else if (ident.contains("*")) {
                final int idx = ident.indexOf('*');
                final Feature<T> left = getInput(ident.substring(0, idx));
                final Feature<T> right = getInput(ident.substring(idx + 1));
                return new Feature.Product<>(left, right, lambda, min, max);
            } else if (ident.endsWith("^2")) {
                final Feature<T> feature = getInput(ident.substring(0, ident.length() - 2));
                return new Feature.Quadratic<>(feature, lambda, min, max);
            } else {
                final Feature<T> feature = getInput(ident);
                return new Feature.Raw<>(feature, lambda, min, max);
            }
        } catch (final NumberFormatException e) {
            throw new MaxentParseException("Malformed number in feature: " + String.join(", ", tokens), e);
        }
    }

    private Feature<T> parseExpression(final String expression, final float lambda, final float min, final float max) throws MaxentParseException {
        final int idxEq = expression.indexOf('=');
        final int idxLt = expression.indexOf('<');
        if (idxEq != -1) {
            final Feature<T> feature = getInput(expression.substring(0, idxEq));
            final float value = Float.parseFloat(expression.substring(idxEq));
            return new Feature.Equal<>(feature, lambda, min, max, value);
        } else if (idxLt != -1) {
            final Feature<T> feature = getInput(expression.substring(idxLt));
            final float threshold = Float.parseFloat(expression.substring(0, idxLt));
            return new Feature.Threshold<>(feature, lambda, min, max, threshold);
        } else {
            throw new MaxentParseException("Missing operator in expression: " + expression);
        }
    }

    private Feature<T> getInput(final String name) throws MaxentParseException {
        return namedInputs.apply(name).orElseThrow(() -> new MaxentParseException("Unrecognized input name: " + name));
    }
}
