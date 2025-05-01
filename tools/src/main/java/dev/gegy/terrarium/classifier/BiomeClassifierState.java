package dev.gegy.terrarium.classifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import dev.gegy.terrarium.Biome;
import dev.gegy.terrarium.Mapper;
import dev.gegy.terrarium.backend.earth.GeoParameters;
import dev.gegy.terrarium.backend.expr.classifier.ClassifierNode;
import dev.gegy.terrarium.backend.expr.classifier.Classifiers;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BiomeClassifierState {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final ClassifierTrainer<GeoParameters, Biome> TRAINER = new ClassifierTrainer<>(GeoParameters.allFeatures(), 10, 5, Mapper.EXECUTOR);

    private static final ClassifierNode<GeoParameters, Biome> DEFAULT_CLASSIFIER = Classifiers.leaf(Biome.PLAINS);

    private Consumer<ClassifierNode<GeoParameters, Biome>> classifierListener = classifier -> {
    };

    private final List<ClassifiedPoint> points = new ArrayList<>();
    private volatile ClassifierNode<GeoParameters, Biome> classifier = DEFAULT_CLASSIFIER;

    public void setClassifierListener(final Consumer<ClassifierNode<GeoParameters, Biome>> classifierListener) {
        this.classifierListener = classifierListener;
        classifierListener.accept(classifier);
    }

    public void addPoints(final List<ClassifiedPoint> points) {
        this.points.addAll(points);
        rebuildClassifier();
    }

    public void addPoint(final ClassifiedPoint point) {
        points.add(point);
        rebuildClassifier();
    }

    public void removePoint(final ClassifiedPoint point) {
        points.remove(point);
        rebuildClassifier();
    }

    public ClassifierNode<GeoParameters, Biome> getClassifier() {
        return classifier;
    }

    public List<ClassifiedPoint> getPoints() {
        return points;
    }

    private void rebuildClassifier() {
        if (points.isEmpty()) {
            updateClassifier(DEFAULT_CLASSIFIER);
            return;
        }
        TRAINER.build(points.stream().map(ClassifiedPoint::toSample).toList())
                .thenAccept(this::updateClassifier);
    }

    private void updateClassifier(final ClassifierNode<GeoParameters, Biome> classifier) {
        if (classifier.equals(this.classifier)) {
            return;
        }
        this.classifier = classifier;
        classifierListener.accept(classifier);
    }

    public static BiomeClassifierState read(final Path pointsPath) {
        final BiomeClassifierState state = new BiomeClassifierState();
        try (final BufferedReader reader = Files.newBufferedReader(pointsPath)) {
            final JsonElement json = JsonParser.parseReader(reader);
            ClassifiedPoint.LIST_CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(err -> LOGGER.error("Failed to parse points: {}", err))
                    .ifPresent(state::addPoints);
        } catch (final IOException e) {
            LOGGER.error("Failed to load points file", e);
        }
        return state;
    }

    public void write(final Path pointsPath) {
        try (final BufferedWriter writer = Files.newBufferedWriter(pointsPath)) {
            final JsonElement json = ClassifiedPoint.LIST_CODEC.encodeStart(JsonOps.INSTANCE, points).getOrThrow();
            GSON.toJson(json, writer);
        } catch (final IOException | JsonIOException e) {
            LOGGER.error("Unable to write classified point data set", e);
        }
    }

    public void exportClassifier(final Path classifierPath) {
        try (final BufferedWriter writer = Files.newBufferedWriter(classifierPath)) {
            final JsonElement json = PredictorCodecs.BIOME_CLASSIFIER.encodeStart(JsonOps.INSTANCE, classifier).getOrThrow();
            GSON.toJson(json, writer);
        } catch (final IOException | JsonIOException e) {
            LOGGER.error("Unable to write classifier", e);
        }
    }
}
