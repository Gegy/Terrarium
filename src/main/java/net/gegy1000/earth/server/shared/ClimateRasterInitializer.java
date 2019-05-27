package net.gegy1000.earth.server.shared;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.util.ProcessTracker;
import net.gegy1000.earth.server.util.ProgressTracker;
import net.gegy1000.earth.server.world.data.source.CachingInput;
import net.gegy1000.earth.server.world.data.source.WorldClimateRaster;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClimateRasterInitializer implements SharedDataInitializer {
    private static final Path JANUARY_PATH = TiledDataSource.GLOBAL_CACHE_ROOT.resolve("january_climate.xz");
    private static final Path JULY_PATH = TiledDataSource.GLOBAL_CACHE_ROOT.resolve("july_climate.xz");

    private static final String JANUARY_URL = "https://terrariumearth.azureedge.net/geo/january_climate.xz";
    private static final String JULY_URL = "https://terrariumearth.azureedge.net/geo/july_climate.xz";

    private static final RasterType JANUARY_RASTER = new RasterType(JANUARY_PATH, JANUARY_URL);
    private static final RasterType JULY_RASTER = new RasterType(JULY_PATH, JULY_URL);

    @Override
    public void initialize(SharedEarthData data, ProcessTracker processTracker) {
        ProgressTracker master = processTracker.push(new TextComponentTranslation("initializer.terrarium.climate_rasters"), 2);

        master.use(() -> {
            WorldClimateRaster january = JANUARY_RASTER.load();
            data.put(SharedEarthData.JANUARY_CLIMATE, january);

            master.step(1);

            WorldClimateRaster july = JULY_RASTER.load();
            data.put(SharedEarthData.JULY_CLIMATE, july);

            master.step(1);
        });
    }

    private static class RasterType {
        private final Path path;
        private final String url;

        RasterType(Path path, String url) {
            this.path = path;
            this.url = url;
        }

        WorldClimateRaster load() throws IOException {
            try (InputStream input = this.getStream()) {
                return WorldClimateRaster.parse(input);
            }
        }

        private InputStream getStream() throws IOException {
            if (Files.exists(this.path)) {
                return Files.newInputStream(this.path);
            }

            return CachingInput.getCachingStream(this.getRemoteStream(), this.path);
        }

        private InputStream getRemoteStream() throws IOException {
            URL url = new URL(this.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", TerrariumEarth.USER_AGENT);

            return connection.getInputStream();
        }
    }
}
