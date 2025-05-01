package dev.gegy.terrarium;

import dev.gegy.terrarium.backend.earth.EarthLayers;
import dev.gegy.terrarium.backend.earth.EarthTiles;
import dev.gegy.terrarium.backend.loader.ConcurrencyLimiter;
import dev.gegy.terrarium.backend.tile.GuavaTileCache;
import dev.gegy.terrarium.map.MapController;
import dev.gegy.terrarium.map.MapPanel;
import dev.gegy.terrarium.map.feature.LandCoverFeature;
import dev.gegy.terrarium.map.feature.MapFeature;
import dev.gegy.terrarium.map.feature.MapTileFeature;
import dev.gegy.terrarium.map.feature.RainfallFeature;
import dev.gegy.terrarium.map.feature.ScalarRasterFeature;
import dev.gegy.terrarium.map.feature.TemperatureFeature;

import javax.swing.*;
import java.awt.*;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class Mapper {
    public static final ForkJoinPool EXECUTOR = ForkJoinPool.commonPool();

    private static final Path CACHE_ROOT = Path.of("tiles");

    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .executor(EXECUTOR)
            .build();

    public static final EarthTiles TILES = new EarthTiles.Config(HTTP_CLIENT, new ConcurrencyLimiter(16), CACHE_ROOT, EXECUTOR, EXECUTOR)
            .create(new GuavaTileCache(Duration.ofMinutes(1), 1024));

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

        final List<FeatureEntry> features = createFeatures();

        final JPanel leftSideBar = new JPanel();
        final JList<FeatureEntry> featureList = new JList<>(features.toArray(FeatureEntry[]::new));
        featureList.setSelectedIndex(0);
        leftSideBar.add(featureList);

        final MapController mapController = new MapController();
        final MapPanel map = new MapPanel(mapController, TILES, EXECUTOR, featureList.getSelectedValue().feature());
        featureList.addListSelectionListener(e -> map.setFeature(featureList.getSelectedValue().feature()));

        frame.getContentPane().add(BorderLayout.WEST, leftSideBar);
        frame.getContentPane().add(BorderLayout.CENTER, map);

        frame.setVisible(true);
    }

    public static List<FeatureEntry> createFeatures() {
        final List<FeatureEntry> features = new ArrayList<>();
        features.add(new FeatureEntry("ESRI Tiles", new MapTileFeature(
                HTTP_CLIENT,
                new ConcurrencyLimiter(2),
                CACHE_ROOT.resolve("esri"),
                tile -> "https://services.arcgisonline.com/ArcGis/rest/services/World_Imagery/MapServer/tile/" + tile.zoom() + "/" + tile.y() + "/" + tile.x() + ".png"
        )));
        features.add(new FeatureEntry("Google Satellite", new MapTileFeature(
                HTTP_CLIENT,
                new ConcurrencyLimiter(2),
                CACHE_ROOT.resolve("google_satellite"),
                tile -> "https://mt1.google.com/vt/lyrs=s&x=" + tile.x() + "&y=" + tile.y() + "&z=" + tile.zoom()
        )));
        features.add(new FeatureEntry("OpenStreetMap", new MapTileFeature(
                HTTP_CLIENT,
                new ConcurrencyLimiter(2),
                CACHE_ROOT.resolve("openstreetmap"),
                tile -> "https://tile.openstreetmap.org/" + tile.zoom() + "/" + tile.x() + "/" + tile.y() + ".png"
        )));
        features.add(new FeatureEntry("OpenTopo", new MapTileFeature(
                HTTP_CLIENT,
                new ConcurrencyLimiter(2),
                CACHE_ROOT.resolve("opentopo"),
                tile -> "https://c.tile.opentopomap.org/" + tile.zoom() + "/" + tile.x() + "/" + tile.y() + ".png"
        )));

        features.add(new FeatureEntry("Elevation", new ScalarRasterFeature(EarthLayers::elevation, ColorRamps.ELEVATION)));
        features.add(new FeatureEntry("Land Cover", new LandCoverFeature()));
        features.add(new FeatureEntry("Soil pH", new ScalarRasterFeature(EarthLayers::soilPh, ColorRamps.PH)));
        features.add(new FeatureEntry("Clay Content", new ScalarRasterFeature(EarthLayers::clayContent, ColorRamps.SOIL)));
        features.add(new FeatureEntry("Silt Content", new ScalarRasterFeature(EarthLayers::siltContent, ColorRamps.SOIL)));
        features.add(new FeatureEntry("Sand Content", new ScalarRasterFeature(EarthLayers::sandContent, ColorRamps.SOIL)));
        features.add(new FeatureEntry("Mean Temperature", new TemperatureFeature(EarthLayers::meanTemperature)));
        features.add(new FeatureEntry("Min Temperature", new TemperatureFeature(EarthLayers::minTemperature)));
        features.add(new FeatureEntry("Annual Rainfall", new RainfallFeature()));

        return features;
    }

    public record FeatureEntry(String name, MapFeature feature) {
        @Override
        public String toString() {
            return name;
        }
    }
}
