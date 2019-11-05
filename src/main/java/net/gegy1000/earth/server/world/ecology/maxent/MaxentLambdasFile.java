package net.gegy1000.earth.server.world.ecology.maxent;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.ecology.GrowthIndicator;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class MaxentLambdasFile {
    private final ImmutableList<GrowthIndicator> features;
    private final Object2DoubleMap<String> fields;

    private MaxentLambdasFile(ImmutableList<GrowthIndicator> features, Object2DoubleMap<String> fields) {
        this.features = features;
        this.fields = fields;
    }

    public static MaxentLambdasFile parse(ResourceLocation location) throws IOException, MaxentParseException {
        String path = "/data/" + location.getNamespace() + "/" + location.getPath();
        try (InputStream input = TerrariumEarth.class.getResourceAsStream(path)) {
            return parse(input);
        }
    }

    public static MaxentLambdasFile parse(InputStream input) throws IOException, MaxentParseException {
        Object2DoubleMap<String> fields = new Object2DoubleOpenHashMap<>();

        ImmutableList.Builder<GrowthIndicator> features = ImmutableList.builder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(", ");
            if (tokens.length == 2) {
                String key = tokens[0];
                double value = Double.parseDouble(tokens[1]);
                fields.put(key, value);
            } else if (tokens.length == 4) {
                GrowthIndicator feature = MaxentLambdasFile.parseFeature(tokens);
                if (feature != null) features.add(feature);
            } else {
                throw new MaxentParseException("Unexpected line format: " + line);
            }
        }

        return new MaxentLambdasFile(features.build(), fields);
    }

    @Nullable
    public static GrowthIndicator parseFeature(String line) throws MaxentParseException {
        return parseFeature(line.split(", "));
    }

    @Nullable
    public static GrowthIndicator parseFeature(String[] tokens) throws MaxentParseException {
        if (tokens.length != 4) throw new MaxentParseException("Invalid number of tokens for feature");

        try {
            String ident = tokens[0];

            double lambda = Double.parseDouble(tokens[1]);
            if (lambda <= 1e-6) return null;

            double min = Double.parseDouble(tokens[2]);
            double max = Double.parseDouble(tokens[3]);

            if (ident.startsWith("(") && ident.endsWith(")")) {
                String expression = ident.substring(1, ident.length() - 1);
                return parseExpressionFeature(expression, lambda, min, max);
            } else if (ident.startsWith("'")) {
                GrowthIndicator predictor = GrowthPredictors.byId(ident.substring(1));
                return MaxentFeatures.hinge(predictor, lambda, min, max);
            } else if (ident.startsWith("`")) {
                GrowthIndicator predictor = GrowthPredictors.byId(ident.substring(1));
                return MaxentFeatures.reverseHinge(predictor, lambda, min, max);
            } else if (ident.contains("*")) {
                int idx = ident.indexOf('*');
                GrowthIndicator left = GrowthPredictors.byId(ident.substring(0, idx));
                GrowthIndicator right = GrowthPredictors.byId(ident.substring(idx));
                return MaxentFeatures.product(left, right, lambda, min, max);
            } else if (ident.contains("^2")) {
                GrowthIndicator predictor = GrowthPredictors.byId(ident.substring(0, ident.length() - 2));
                return MaxentFeatures.quadratic(predictor, lambda, min, max);
            } else {
                GrowthIndicator predictor = GrowthPredictors.byId(ident);
                return MaxentFeatures.raw(predictor, lambda, min, max);
            }
        } catch (NumberFormatException e) {
            throw new MaxentParseException("Malformed number", e);
        }
    }

    private static GrowthIndicator parseExpressionFeature(String expression, double lambda, double min, double max) throws MaxentParseException {
        int idxEq = expression.indexOf('=');
        int idxLt = expression.indexOf('<');
        if (idxEq != -1) {
            GrowthIndicator predictor = GrowthPredictors.byId(expression.substring(0, idxEq));
            double value = Double.parseDouble(expression.substring(idxEq));
            return MaxentFeatures.equal(predictor, lambda, min, max, value);
        } else if (idxLt != -1) {
            GrowthIndicator predictor = GrowthPredictors.byId(expression.substring(idxLt));
            double threshold = Double.parseDouble(expression.substring(0, idxLt));
            return MaxentFeatures.threshold(predictor, lambda, min, max, threshold);
        } else {
            throw new MaxentParseException("Invalid expression operator");
        }
    }

    public ImmutableList<GrowthIndicator> getFeatures() {
        return this.features;
    }

    public boolean isEmpty() {
        return this.features.isEmpty();
    }

    public double getFieldOr(String key, double or) {
        if (this.fields.containsKey(key)) {
            return this.fields.getDouble(key);
        } else {
            return or;
        }
    }
}
