package dev.gegy.terrarium;

import dev.gegy.terrarium.backend.earth.EarthLayers;
import dev.gegy.terrarium.backend.earth.EarthTiles;
import dev.gegy.terrarium.backend.loader.ConcurrencyLimiter;
import dev.gegy.terrarium.backend.tile.GuavaTileCache;

import javax.swing.*;
import java.awt.*;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class Map {
    public static final ForkJoinPool EXECUTOR = ForkJoinPool.commonPool();

    private static final List<FeatureEntry> FEATURES = List.of(
            new FeatureEntry("Elevation", new ScalarRasterFeature(EarthLayers::elevation, ColorRamps.ELEVATION)),
            new FeatureEntry("Soil pH", new ScalarRasterFeature(EarthLayers::soilPh, ColorRamps.PH)),
            new FeatureEntry("Clay Content", new ScalarRasterFeature(EarthLayers::clayContent, ColorRamps.SOIL)),
            new FeatureEntry("Silt Content", new ScalarRasterFeature(EarthLayers::siltContent, ColorRamps.SOIL)),
            new FeatureEntry("Sand Content", new ScalarRasterFeature(EarthLayers::sandContent, ColorRamps.SOIL)),
            new FeatureEntry("Mean Temperature", new TemperatureFeature(EarthLayers::meanTemperature)),
            new FeatureEntry("Min Temperature", new TemperatureFeature(EarthLayers::minTemperature)),
            new FeatureEntry("Annual Rainfall", new RainfallFeature(EarthLayers::annualRainfall))
    );

    public static void main(final String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        final JFrame frame = new JFrame("Map");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1600, 900);

        final HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .executor(EXECUTOR)
                .build();

        final Path cacheRoot = Path.of("tiles");
        final EarthTiles tiles = new EarthTiles.Config(httpClient, new ConcurrencyLimiter(16), cacheRoot, EXECUTOR, EXECUTOR)
                .create(new GuavaTileCache(Duration.ofMinutes(1), 1024));

        final JPanel leftSideBar = new JPanel();
        final JList<FeatureEntry> featureList = new JList<>(FEATURES.toArray(FeatureEntry[]::new));
        featureList.setSelectedIndex(0);
        leftSideBar.add(featureList);

        final MapController mapController = new MapController();
        final MapPanel map = new MapPanel(mapController, tiles, EXECUTOR, featureList.getSelectedValue().feature());
        featureList.addListSelectionListener(e -> map.setFeature(featureList.getSelectedValue().feature()));

        frame.getContentPane().add(BorderLayout.WEST, leftSideBar);
        frame.getContentPane().add(BorderLayout.CENTER, map);

        frame.setVisible(true);
    }

    private record FeatureEntry(String name, MapFeature feature) {
        @Override
        public String toString() {
            return name;
        }
    }
}
