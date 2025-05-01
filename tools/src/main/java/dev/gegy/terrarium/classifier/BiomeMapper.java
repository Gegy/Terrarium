package dev.gegy.terrarium.classifier;

import dev.gegy.terrarium.Biome;
import dev.gegy.terrarium.Mapper;
import dev.gegy.terrarium.backend.earth.GeoParameters;
import dev.gegy.terrarium.backend.expr.classifier.Classifier;
import dev.gegy.terrarium.backend.expr.classifier.ClassifierNode;
import dev.gegy.terrarium.map.MapController;
import dev.gegy.terrarium.map.MapDecoration;
import dev.gegy.terrarium.map.MapPanel;
import dev.gegy.terrarium.map.feature.BiomeClassifierFeature;
import dev.gegy.terrarium.map.feature.MapFeature;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class BiomeMapper {
    private static final Executor UI_EXECUTOR = EventQueue::invokeLater;

    private static final Path CLASSIFIED_POINTS_PATH = Path.of("classified_points.json");
    private static final Path CLASSIFIER_PATH = Path.of("classifier.json");

    public static void main(final String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException |
                       UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        final JFrame frame = new JFrame("Map");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1600, 900);

        final List<Mapper.FeatureEntry> features = Mapper.createFeatures();

        final JPanel leftSideBar = new JPanel();
        final JList<Mapper.FeatureEntry> featureList = new JList<>(features.toArray(Mapper.FeatureEntry[]::new));
        featureList.setSelectedIndex(0);
        leftSideBar.add(featureList);

        final JPanel rightSideBar = new JPanel();
        final JList<Biome> biomeList = new JList<>(Biome.values());
        biomeList.setSelectedIndex(0);
        rightSideBar.add(biomeList);

        final MapController mapController = new MapController();

        final MapPanel referenceMap = new MapPanel(mapController, Mapper.TILES, Mapper.EXECUTOR, featureList.getSelectedValue().feature());
        featureList.addListSelectionListener(e -> referenceMap.setFeature(featureList.getSelectedValue().feature()));

        final BiomeClassifierState classifierState = Files.exists(CLASSIFIED_POINTS_PATH) ? BiomeClassifierState.read(CLASSIFIED_POINTS_PATH) : new BiomeClassifierState();

        final MapPanel classifierMap = createClassifierMap(mapController, classifierState);

        final PointSampler pointSampler = new PointSampler(Mapper.TILES);

        final MapPanel.MouseClickListener mapClick = (event, latitude, longitude) -> {
            if (event.getButton() == MouseEvent.BUTTON1) {
                classifyBiomeAtPoint(latitude, longitude, biomeList.getSelectedValue(), pointSampler, classifierState, classifierMap);
            } else if (event.getButton() == MouseEvent.BUTTON2) {
                pickBiomeAtPoint(latitude, longitude, pointSampler, classifierState).thenAcceptAsync(maybeBiome -> maybeBiome.ifPresent(biome ->
                        biomeList.setSelectedValue(biome, true)
                ), UI_EXECUTOR);
            }
        };
        classifierMap.setMouseClickListener(mapClick);
        referenceMap.setMouseClickListener(mapClick);

        frame.getContentPane().add(BorderLayout.WEST, leftSideBar);
        final JPanel mapContainer = new JPanel(new GridLayout(1, 2));
        mapContainer.add(referenceMap);
        mapContainer.add(classifierMap);
        frame.getContentPane().add(BorderLayout.CENTER, mapContainer);
        frame.getContentPane().add(BorderLayout.EAST, rightSideBar);

        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                classifierState.write(CLASSIFIED_POINTS_PATH);
                classifierState.exportClassifier(CLASSIFIER_PATH);
            }
        });
    }

    private static void classifyBiomeAtPoint(final double latitude, final double longitude, final Biome biome, final PointSampler pointSampler, final BiomeClassifierState classifierState, final MapPanel classifierMap) {
        pointSampler.sample(latitude, longitude).thenAcceptAsync(sample -> {
            if (sample.isEmpty()) {
                return;
            }
            final ClassifiedPoint point = new ClassifiedPoint(latitude, longitude, sample.get(), biome);
            if (point.parameters().elevation() >= 0.0f) {
                classifierState.addPoint(point);
                classifierMap.addDecoration(new BiomeMarker(classifierState, point));
            }
        }, UI_EXECUTOR);
    }

    private static CompletableFuture<Optional<Biome>> pickBiomeAtPoint(final double latitude, final double longitude, final PointSampler pointSampler, final BiomeClassifierState classifierState) {
        return pointSampler.sample(latitude, longitude).thenApply(sample -> {
            if (sample.isEmpty()) {
                return Optional.empty();
            }
            final Classifier<GeoParameters, Biome> classifier = ClassifierNode.compile(classifierState.getClassifier());
            return Optional.of(classifier.evaluate(sample.get()));
        });
    }

    private static MapPanel createClassifierMap(final MapController mapController, final BiomeClassifierState classifierState) {
        final MapPanel classifierMap = new MapPanel(mapController, Mapper.TILES, Mapper.EXECUTOR, MapFeature.EMPTY);
        for (final ClassifiedPoint point : classifierState.getPoints()) {
            classifierMap.addDecoration(new BiomeMarker(classifierState, point));
        }
        classifierState.setClassifierListener(classifier -> UI_EXECUTOR.execute(() ->
                classifierMap.setFeature(new BiomeClassifierFeature(classifier))
        ));
        return classifierMap;
    }

    private static class BiomeMarker implements MapDecoration {
        private final BiomeClassifierState biomeClassifierState;
        private final ClassifiedPoint point;
        private final Color color;

        public BiomeMarker(final BiomeClassifierState biomeClassifierState, final ClassifiedPoint point) {
            this.biomeClassifierState = biomeClassifierState;
            this.point = point;
            color = new Color(point.biome().color());
        }

        @Override
        public double latitude() {
            return point.latitude();
        }

        @Override
        public double longitude() {
            return point.longitude();
        }

        @Override
        public int pickRadius() {
            return 6;
        }

        @Override
        public ClickResponse clicked(final MouseEvent event) {
            if (event.getButton() == MouseEvent.BUTTON3) {
                biomeClassifierState.removePoint(point);
                return ClickResponse.REMOVE;
            }
            return ClickResponse.NONE;
        }

        @Override
        public void draw(final Graphics2D graphics, final int x, final int y) {
            graphics.setColor(Color.BLACK);
            graphics.fillOval(x - 4, y - 4, 8, 8);
            graphics.setColor(color);
            graphics.fillOval(x - 2, y - 2, 4, 4);
        }
    }
}
