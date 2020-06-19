package net.gegy1000.earth.server.world.ecology.maxent;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.maxent.feature.MaxentFeature;
import net.gegy1000.earth.server.world.ecology.maxent.feature.MaxentFeatures;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class MaxentLambdasFile {
    private final ImmutableList<MaxentFeature> features;
    private final Object2FloatMap<String> fields;

    private MaxentLambdasFile(ImmutableList<MaxentFeature> features, Object2FloatMap<String> fields) {
        this.features = features;
        this.fields = fields;
    }

    public static MaxentLambdasFile parse(ResourceLocation location) throws IOException, MaxentParseException {
        String path = "/data/" + location.getNamespace() + "/" + location.getPath();
        try (InputStream input = TerrariumEarth.class.getResourceAsStream(path)) {
            return parse(input);
        }
    }

    // TODO: Clamp predictors based on range specified by linear features?
    public static MaxentLambdasFile parse(InputStream input) throws IOException, MaxentParseException {
        Object2FloatMap<String> fields = new Object2FloatOpenHashMap<>();

        ImmutableList.Builder<MaxentFeature> features = ImmutableList.builder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(", ");
            if (tokens.length == 2) {
                String key = tokens[0];
                float value = Float.parseFloat(tokens[1]);
                fields.put(key, value);
            } else if (tokens.length == 4) {
                MaxentFeature feature = MaxentLambdasFile.parseFeature(tokens);
                if (feature != null) features.add(feature);
            } else {
                throw new MaxentParseException("Unexpected line format: " + line);
            }
        }

        return new MaxentLambdasFile(features.build(), fields);
    }

    @Nullable
    public static MaxentFeature parseFeature(String line) throws MaxentParseException {
        return parseFeature(line.split(", "));
    }

    @Nullable
    public static MaxentFeature parseFeature(String[] tokens) throws MaxentParseException {
        if (tokens.length != 4) throw new MaxentParseException("Invalid number of tokens for feature");

        try {
            String ident = tokens[0];

            float lambda = Float.parseFloat(tokens[1]);
            if (Math.abs(lambda) <= 1e-2F) return null;

            float min = Float.parseFloat(tokens[2]);
            float max = Float.parseFloat(tokens[3]);

            if (ident.startsWith("(") && ident.endsWith(")")) {
                String expression = ident.substring(1, ident.length() - 1);
                return parseExpressionFeature(expression, lambda, min, max);
            } else if (ident.startsWith("'")) {
                MaxentFeature feature = GrowthPredictors.featureById(ident.substring(1));
                return MaxentFeatures.hinge(feature, lambda, min, max);
            } else if (ident.startsWith("`")) {
                MaxentFeature feature = GrowthPredictors.featureById(ident.substring(1));
                return MaxentFeatures.reverseHinge(feature, lambda, min, max);
            } else if (ident.contains("*")) {
                int idx = ident.indexOf('*');
                MaxentFeature left = GrowthPredictors.featureById(ident.substring(0, idx));
                MaxentFeature right = GrowthPredictors.featureById(ident.substring(idx + 1));
                return MaxentFeatures.product(left, right, lambda, min, max);
            } else if (ident.contains("^2")) {
                MaxentFeature feature = GrowthPredictors.featureById(ident.substring(0, ident.length() - 2));
                return MaxentFeatures.quadratic(feature, lambda, min, max);
            } else {
                MaxentFeature feature = GrowthPredictors.featureById(ident);
                return MaxentFeatures.raw(feature, lambda, min, max);
            }
        } catch (NumberFormatException e) {
            throw new MaxentParseException("Malformed number", e);
        }
    }

    private static MaxentFeature parseExpressionFeature(String expression, float lambda, float min, float max) throws MaxentParseException {
        int idxEq = expression.indexOf('=');
        int idxLt = expression.indexOf('<');
        if (idxEq != -1) {
            MaxentFeature feature = GrowthPredictors.featureById(expression.substring(0, idxEq));
            float value = Float.parseFloat(expression.substring(idxEq));
            return MaxentFeatures.equal(feature, lambda, min, max, value);
        } else if (idxLt != -1) {
            MaxentFeature feature = GrowthPredictors.featureById(expression.substring(idxLt));
            float threshold = Float.parseFloat(expression.substring(0, idxLt));
            return MaxentFeatures.threshold(feature, lambda, min, max, threshold);
        } else {
            throw new MaxentParseException("Invalid expression operator");
        }
    }

    public ImmutableList<MaxentFeature> getFeatures() {
        return this.features;
    }

    public boolean isEmpty() {
        return this.features.isEmpty();
    }

    public float getFieldOr(String key, float or) {
        if (this.fields.containsKey(key)) {
            return this.fields.getFloat(key);
        } else {
            return or;
        }
    }
}
